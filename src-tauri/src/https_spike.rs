/// spike temporal, borrar en la Fase 3
/// Hace dos GET con timeout de 10 s:
///   (1) https://example.com/  (esperado: OK)
///   (2) https://expired.badssl.com/  (esperado: error de certificado)
use reqwest::Client;
use std::error::Error;
use std::env::consts;
use std::time::Instant;
use webpki_roots::TLS_SERVER_ROOTS;
use rustls::{ClientConfig, RootCertStore};

pub async fn run() -> Result<(), reqwest::Error> {
    let target_os = consts::OS;
    let target_arch = consts::ARCH;
    println!("HTTPSSPIKE rust: start target={}-{}", target_os, target_arch);

    // Build rustls ClientConfig with embedded root certificates only.
    // This disables the platform verifier (which requires JNI on Android)
    // and uses Mozilla's CA root store compiled-in via webpki-roots.
    let root_store = RootCertStore {
        roots: TLS_SERVER_ROOTS.to_vec(),
    };

    let tls_config = ClientConfig::builder()
        .with_root_certificates(root_store)
        .with_no_client_auth();

    let client = Client::builder()
        .timeout(std::time::Duration::from_secs(10))
        .use_preconfigured_tls(tls_config)
        .build()
        .map_err(|e| {
            println!("HTTPSSPIKE rust: client_build error={}", e);
            e
        })?;

    // (1) URL esperada OK
    let instant = Instant::now();
    let resp = client.get("https://example.com/").send().await?;
    let elapsed = instant.elapsed().as_millis();
    let status = resp.status();
    let bytes = resp.bytes().await?.len();
    println!(
        "HTTPSSPIKE rust: probe=valid url=https://example.com/ status={} bytes={} elapsed_ms={}",
        status, bytes, elapsed
    );

    // (2) URL con certificado expirado
    let instant = Instant::now();
    let resp2 = client.get("https://expired.badssl.com/").send().await;
    match resp2 {
        Ok(r) => {
            let status = r.status();
            let bytes = r.bytes().await?.len();
            let elapsed = instant.elapsed().as_millis();
            println!(
                "HTTPSSPIKE rust: probe=valid url=https://expired.badssl.com/ status={} bytes={} elapsed_ms={}",
                status, bytes, elapsed
            );
        }
        Err(e) => {
            // Imprimir el mensaje completo, incluida la cadena de causas
            let mut cause = e.source();
            let mut msg = e.to_string();
            while let Some(c) = cause {
                msg = format!("{} -> {}", msg, c);
                cause = c.source();
            }
            println!(
                "HTTPSSPIKE rust: probe=expired url=https://expired.badssl.com/ error={}",
                msg
            );
        }
    }

    Ok(())
}