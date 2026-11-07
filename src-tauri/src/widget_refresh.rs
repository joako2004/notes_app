//! Explicit refresh request from the Rust process to the widget process.

use std::fmt;

#[cfg(target_os = "android")]
const REFRESH_ACTION: &str = "com.joako2004.notesapp.widget.ACTION_REFRESH_LIST";
#[cfg(target_os = "android")]
const PROVIDER_PACKAGE: &str = "com.joako2004.notesapp";
#[cfg(target_os = "android")]
const PROVIDER_CLASS: &str = "com.joako2004.notesapp.widget.NotesWidgetProvider";

#[derive(Debug)]
pub struct WidgetRefreshError {
    message: String,
}

impl WidgetRefreshError {
    #[cfg(target_os = "android")]
    fn jni(error: jni::errors::Error) -> Self {
        Self {
            message: error.to_string(),
        }
    }
}

impl fmt::Display for WidgetRefreshError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(formatter, "{}", self.message)
    }
}

impl std::error::Error for WidgetRefreshError {}

#[cfg(target_os = "android")]
impl From<jni::errors::Error> for WidgetRefreshError {
    fn from(error: jni::errors::Error) -> Self {
        Self::jni(error)
    }
}

/// Sends an explicit broadcast so the widget process reloads its list.
#[cfg(target_os = "android")]
pub fn request_widget_refresh() -> Result<(), WidgetRefreshError> {
    use jni::JavaVM;

    let android_context = ndk_context::android_context();
    // SAFETY: ndk-context stores the JavaVM pointer initialized by tao before Tauri's setup.
    let vm = unsafe { JavaVM::from_raw(android_context.vm().cast()) }?;
    let mut env = vm.attach_current_thread()?;

    let result = send_refresh_broadcast(&mut env, android_context.context());

    if result.is_err() {
        clear_pending_exception(&mut env);
    }

    result
}

/// No-op outside Android, where the widget process does not exist.
#[cfg(not(target_os = "android"))]
pub fn request_widget_refresh() -> Result<(), WidgetRefreshError> {
    Ok(())
}

#[cfg(target_os = "android")]
fn send_refresh_broadcast(
    env: &mut jni::JNIEnv<'_>,
    raw_context: *mut std::ffi::c_void,
) -> Result<(), WidgetRefreshError> {
    use jni::objects::{JObject, JValue};

    let action = env.new_string(REFRESH_ACTION)?;
    let package = env.new_string(PROVIDER_PACKAGE)?;
    let class = env.new_string(PROVIDER_CLASS)?;

    let intent_class = env.find_class("android/content/Intent")?;
    let intent = env.new_object(
        &intent_class,
        "(Ljava/lang/String;)V",
        &[JValue::Object(action.as_ref())],
    )?;

    let component_class = env.find_class("android/content/ComponentName")?;
    let component = env.new_object(
        &component_class,
        "(Ljava/lang/String;Ljava/lang/String;)V",
        &[
            JValue::Object(package.as_ref()),
            JValue::Object(class.as_ref()),
        ],
    )?;

    env.call_method(
        &intent,
        "setComponent",
        "(Landroid/content/ComponentName;)Landroid/content/Intent;",
        &[JValue::Object(&component)],
    )?;

    // SAFETY: ndk-context owns this application Context global reference for the process lifetime.
    let context = unsafe { JObject::from_raw(raw_context.cast()) };
    env.call_method(
        &context,
        "sendBroadcast",
        "(Landroid/content/Intent;)V",
        &[JValue::Object(&intent)],
    )?;

    Ok(())
}

#[cfg(target_os = "android")]
fn clear_pending_exception(env: &mut jni::JNIEnv<'_>) {
    match env.exception_check() {
        Ok(true) => {
            if let Err(error) = env.exception_describe() {
                println!("SQLITESPIKE rust: exception_describe_error={error}");
            }
            if let Err(error) = env.exception_clear() {
                println!("SQLITESPIKE rust: exception_clear_error={error}");
            }
        }
        Ok(false) => {}
        Err(error) => println!("SQLITESPIKE rust: exception_check_error={error}"),
    }
}
