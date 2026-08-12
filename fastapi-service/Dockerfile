FROM python:3.12-slim

WORKDIR /app

# Copy requirements first to leverage Docker layer caching
COPY requirements.txt .

RUN mkdir -p  /app/outputs
# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application files
COPY app/ ./app/
COPY models/ ./models/
COPY run.py .

# FastAPI port
EXPOSE 8000

# Start the application
CMD ["python", "run.py"]