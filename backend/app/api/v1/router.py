from app.api.v1.endpoints.note import router as note_router

api_router = APIRouter()
api_router.include_router(note_router)