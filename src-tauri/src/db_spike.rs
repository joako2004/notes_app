use std::fs;
use std::time::Duration;

use sqlx::sqlite::{
    SqliteConnectOptions, SqliteJournalMode, SqlitePool, SqlitePoolOptions, SqliteSynchronous,
};
use tauri::{AppHandle, Manager};

const DB_DIR: &str = "databases";
const DB_FILE: &str = "notes_spike.db";
const DEVICE_ID: &str = "spike-device";
const SPIKE_UPDATED_AT: i64 = 1_767_225_600;

struct TaskCounts {
    data_version: i64,
    pending: i64,
    done: i64,
}

const SPIKE_TASKS: [(&str, &str, &str); 4] = [
    (
        "11111111-1111-4111-8111-111111111111",
        "Tomar agua",
        "pending",
    ),
    (
        "22222222-2222-4222-8222-222222222222",
        "Leer 20 minutos",
        "pending",
    ),
    ("33333333-3333-4333-8333-333333333333", "Estirar", "pending"),
    (
        "44444444-4444-4444-8444-444444444444",
        "Revisar correo",
        "done",
    ),
];

pub async fn init(app: &AppHandle) -> Result<SqlitePool, Box<dyn std::error::Error>> {
    let data_dir = app.path().app_data_dir()?;
    let db_dir = data_dir.join(DB_DIR);
    fs::create_dir_all(&db_dir)?;
    let db_path = db_dir.join(DB_FILE);

    let options = SqliteConnectOptions::new()
        .filename(&db_path)
        .create_if_missing(true)
        .journal_mode(SqliteJournalMode::Wal)
        .busy_timeout(Duration::from_secs(5))
        .foreign_keys(true)
        .synchronous(SqliteSynchronous::Normal);

    let pool = SqlitePoolOptions::new()
        .max_connections(1)
        .connect_with(options)
        .await?;

    sqlx::query(
        "CREATE TABLE IF NOT EXISTS tasks (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            status TEXT NOT NULL,
            updated_at INTEGER NOT NULL,
            deleted_at INTEGER,
            device_id TEXT NOT NULL
        )",
    )
    .execute(&pool)
    .await?;

    let row_count: i64 = sqlx::query_scalar("SELECT COUNT(*) FROM tasks")
        .fetch_one(&pool)
        .await?;

    if row_count == 0 {
        let mut transaction = pool.begin().await?;
        for (id, title, status) in SPIKE_TASKS {
            sqlx::query(
                "INSERT INTO tasks (id, title, status, updated_at, deleted_at, device_id)
                 VALUES (?, ?, ?, ?, NULL, ?)",
            )
            .bind(id)
            .bind(title)
            .bind(status)
            .bind(SPIKE_UPDATED_AT)
            .bind(DEVICE_ID)
            .execute(&mut *transaction)
            .await?;
        }
        transaction.commit().await?;
    }

    let sqlite_version: String = sqlx::query_scalar("SELECT sqlite_version()")
        .fetch_one(&pool)
        .await?;
    let journal_mode: String = sqlx::query_scalar("PRAGMA journal_mode")
        .fetch_one(&pool)
        .await?;
    let final_row_count: i64 = sqlx::query_scalar("SELECT COUNT(*) FROM tasks")
        .fetch_one(&pool)
        .await?;

    println!("SQLITESPIKE rust: path={}", db_path.display());
    println!("SQLITESPIKE rust: pid={}", std::process::id());
    println!("SQLITESPIKE rust: sqlite_version={sqlite_version}");
    println!("SQLITESPIKE rust: journal_mode={journal_mode}");
    println!("SQLITESPIKE rust: rows={final_row_count}");

    let watcher_pool = pool.clone();
    match std::thread::Builder::new()
        .name("sqlite-spike-watch".to_string())
        .spawn(move || watch_task_counts(watcher_pool))
    {
        Ok(_) => println!("SQLITESPIKE rust: watcher=started"),
        Err(error) => println!("SQLITESPIKE rust: watcher_spawn_error={error}"),
    }

    #[cfg(target_os = "android")]
    spawn_demo_flip(pool.clone());

    Ok(pool)
}

fn watch_task_counts(pool: SqlitePool) {
    let mut previous: Option<(i64, i64, i64)> = None;
    loop {
        match read_task_counts(&pool) {
            Ok(counts) => {
                let current = (counts.data_version, counts.pending, counts.done);
                if previous != Some(current) {
                    println!(
                        "SQLITESPIKE rust: change data_version={} pending={} done={}",
                        counts.data_version, counts.pending, counts.done
                    );
                    previous = Some(current);
                }
            }
            Err(error) => {
                println!("SQLITESPIKE rust: query_error={error}");
            }
        }
        std::thread::sleep(Duration::from_secs(2));
    }
}

fn read_task_counts(pool: &SqlitePool) -> Result<TaskCounts, sqlx::Error> {
    let (data_version, status_counts) = tauri::async_runtime::block_on(async {
        let data_version: i64 = sqlx::query_scalar("PRAGMA data_version")
            .fetch_one(pool)
            .await?;
        let status_counts: Vec<(String, i64)> = sqlx::query_as::<_, (String, i64)>(
            "SELECT status, COUNT(*) FROM tasks WHERE deleted_at IS NULL GROUP BY status",
        )
        .fetch_all(pool)
        .await?;
        Ok::<(i64, Vec<(String, i64)>), sqlx::Error>((data_version, status_counts))
    })?;

    let mut pending = 0;
    let mut done = 0;
    for (status, count) in status_counts {
        match status.as_str() {
            "pending" => pending = count,
            "done" => done = count,
            _ => {}
        }
    }

    Ok(TaskCounts {
        data_version,
        pending,
        done,
    })
}

// spike temporal, borrar en la Fase 2
#[cfg(target_os = "android")]
const DEMO_TASK_ID: &str = "44444444-4444-4444-8444-444444444444";

#[cfg(target_os = "android")]
fn spawn_demo_flip(pool: SqlitePool) {
    match std::thread::Builder::new()
        .name("sqlite-spike-demo".to_string())
        .spawn(move || demo_flip_loop(pool))
    {
        Ok(_) => println!("SQLITESPIKE rust: demo=started"),
        Err(error) => println!("SQLITESPIKE rust: demo_spawn_error={error}"),
    }
}

#[cfg(target_os = "android")]
fn demo_flip_loop(pool: SqlitePool) {
    loop {
        std::thread::sleep(Duration::from_secs(20));
        let status = match flip_demo_task(&pool) {
            Ok(status) => status,
            Err(error) => {
                println!("SQLITESPIKE rust: demo_error={error}");
                continue;
            }
        };
        let refresh = match crate::widget_refresh::request_widget_refresh() {
            Ok(()) => "ok".to_string(),
            Err(error) => format!("error: {error}"),
        };
        println!("SQLITESPIKE rust: demo_flip id={DEMO_TASK_ID} status={status} refresh={refresh}");
    }
}

#[cfg(target_os = "android")]
#[derive(Debug)]
enum DemoFlipError {
    Database(sqlx::Error),
    Clock(std::time::SystemTimeError),
    TaskNotFound,
}

#[cfg(target_os = "android")]
impl std::fmt::Display for DemoFlipError {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Database(error) => write!(formatter, "database error: {error}"),
            Self::Clock(error) => write!(formatter, "clock error: {error}"),
            Self::TaskNotFound => write!(formatter, "demo task not found"),
        }
    }
}

#[cfg(target_os = "android")]
impl std::error::Error for DemoFlipError {}

#[cfg(target_os = "android")]
impl From<sqlx::Error> for DemoFlipError {
    fn from(error: sqlx::Error) -> Self {
        Self::Database(error)
    }
}

#[cfg(target_os = "android")]
impl From<std::time::SystemTimeError> for DemoFlipError {
    fn from(error: std::time::SystemTimeError) -> Self {
        Self::Clock(error)
    }
}

#[cfg(target_os = "android")]
fn flip_demo_task(pool: &SqlitePool) -> Result<String, DemoFlipError> {
    tauri::async_runtime::block_on(async {
        let current: String = sqlx::query_scalar("SELECT status FROM tasks WHERE id = ?")
            .bind(DEMO_TASK_ID)
            .fetch_one(pool)
            .await?;
        let next = if current == "done" { "pending" } else { "done" };
        let updated_at = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)?
            .as_secs() as i64;
        let outcome = sqlx::query("UPDATE tasks SET status = ?, updated_at = ? WHERE id = ?")
            .bind(next)
            .bind(updated_at)
            .bind(DEMO_TASK_ID)
            .execute(pool)
            .await?;
        if outcome.rows_affected() != 1 {
            return Err(DemoFlipError::TaskNotFound);
        }
        Ok(next.to_string())
    })
}
