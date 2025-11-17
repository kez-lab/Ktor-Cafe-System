#!/bin/bash

# Google Cloud Platform Deployment Script for Ktor Application
# This script automates the deployment to Google Cloud Run

set -e  # Exit on error

# ============================================
# Configuration
# ============================================

PROJECT_ID="bubbly-cascade-419802"
REGION="asia-northeast3"  # Seoul region
SERVICE_NAME="ktor-app"
IMAGE_NAME="ktor-app"
REPOSITORY_NAME="ktor-repo"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ============================================
# Helper Functions
# ============================================

info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# ============================================
# Validation
# ============================================

info "Validating Google Cloud configuration..."

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    error "gcloud CLI is not installed. Please install from: https://cloud.google.com/sdk/docs/install"
fi

# Check if authenticated
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" &> /dev/null; then
    error "Not authenticated. Please run: gcloud auth login"
fi

# Set project
gcloud config set project "$PROJECT_ID"
info "Using project: $PROJECT_ID"

# ============================================
# Enable Required APIs
# ============================================

info "Enabling required Google Cloud APIs..."

gcloud services enable \
    cloudbuild.googleapis.com \
    run.googleapis.com \
    artifactregistry.googleapis.com \
    --project="$PROJECT_ID" \
    --quiet

info "APIs enabled successfully"

# ============================================
# Create Artifact Registry Repository
# ============================================

info "Setting up Artifact Registry..."

# Check if repository exists
if gcloud artifacts repositories describe "$REPOSITORY_NAME" \
    --location="$REGION" \
    --project="$PROJECT_ID" &> /dev/null; then
    info "Repository '$REPOSITORY_NAME' already exists"
else
    info "Creating Artifact Registry repository..."
    gcloud artifacts repositories create "$REPOSITORY_NAME" \
        --repository-format=docker \
        --location="$REGION" \
        --description="Docker repository for Ktor application" \
        --project="$PROJECT_ID"
    info "Repository created successfully"
fi

# ============================================
# Configure Docker for Artifact Registry
# ============================================

info "Configuring Docker authentication..."

gcloud auth configure-docker "$REGION-docker.pkg.dev" --quiet

# ============================================
# Build and Push Docker Image
# ============================================

IMAGE_TAG="$REGION-docker.pkg.dev/$PROJECT_ID/$REPOSITORY_NAME/$IMAGE_NAME:latest"
IMAGE_TAG_SHA="$REGION-docker.pkg.dev/$PROJECT_ID/$REPOSITORY_NAME/$IMAGE_NAME:$(git rev-parse --short HEAD 2>/dev/null || echo 'local')"

info "Building Docker image for linux/amd64 platform..."
docker build --platform linux/amd64 -t "$IMAGE_TAG" -t "$IMAGE_TAG_SHA" .

info "Pushing image to Artifact Registry..."
docker push "$IMAGE_TAG"
docker push "$IMAGE_TAG_SHA"

info "Image pushed successfully: $IMAGE_TAG"

# ============================================
# Deploy to Cloud Run
# ============================================

info "Deploying to Cloud Run..."

# Check if .env file exists for environment variables
if [ -f .env ]; then
    # Extract DATABASE_URL from .env
    DATABASE_URL=$(grep '^DATABASE_URL=' .env | cut -d '=' -f2-)

    gcloud run deploy "$SERVICE_NAME" \
        --image="$IMAGE_TAG" \
        --platform=managed \
        --region="$REGION" \
        --allow-unauthenticated \
        --set-env-vars="DATABASE_URL=$DATABASE_URL" \
        --memory=512Mi \
        --cpu=1 \
        --min-instances=0 \
        --max-instances=10 \
        --timeout=300 \
        --port=8080 \
        --project="$PROJECT_ID"
else
    warn "No .env file found. Deploying without DATABASE_URL"

    gcloud run deploy "$SERVICE_NAME" \
        --image="$IMAGE_TAG" \
        --platform=managed \
        --region="$REGION" \
        --allow-unauthenticated \
        --memory=512Mi \
        --cpu=1 \
        --min-instances=0 \
        --max-instances=10 \
        --timeout=300 \
        --port=8080 \
        --project="$PROJECT_ID"
fi

# ============================================
# Get Service URL
# ============================================

SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" \
    --platform=managed \
    --region="$REGION" \
    --format='value(status.url)' \
    --project="$PROJECT_ID")

# ============================================
# Summary
# ============================================

echo ""
info "================================"
info "Deployment completed successfully!"
info "================================"
echo ""
info "Service URL: $SERVICE_URL"
info "Image: $IMAGE_TAG"
info "Region: $REGION"
echo ""
info "Test your application:"
echo "  curl $SERVICE_URL"
echo ""
info "View logs:"
echo "  gcloud run services logs tail $SERVICE_NAME --region=$REGION"
echo ""
