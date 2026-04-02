from fastapi import FastAPI
 
app = FastAPI(
    title="ML Policy Engine — ZeroTrust Auth Platform",
    description="Anomaly detection and ABAC policy evaluation service",
    version="0.1.0",
)
 
 
@app.get("/health")
async def health():
    return {"status": "healthy"}