from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.core.db import create_db_and_tables

app = FastAPI(
    title="Notes App API",
    version="1.0.0",
    description="API backend para la aplicación Notes App",
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Health check endpoint
@app.get("/healthz", include_in_schema=False)
async def health_check():
    return {"status": "ok"}

# Create database tables on startup
@app.on_event("startup")
def on_startup():
    create_db_and_tables()

# Include API routers
app.include_router(api_router, prefix=f"{settings.API_V1_STR}")