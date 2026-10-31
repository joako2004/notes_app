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

    Ok(pool)
}
