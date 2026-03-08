# Ingest and Transcode Orchestration Script for CognitiveStream
# Usage: ./ingest_and_transcode.ps1 -YoutubeKey "dQw4w9WgXcQ" -OutputDir "storage/hls-content/movie-123"

param (
    [Parameter(Mandatory=$true)]
    [string]$YoutubeKey,

    [Parameter(Mandatory=$true)]
    [string]$OutputDir
)

if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force
}

$TempFile = Join-Path $OutputDir "temp_input.mp4"
$TranscodeScript = Join-Path (Get-Location) "scripts/transcode.ps1"

Write-Host "[Ingestion] Stage 1: Downloading source for key: $YoutubeKey" -ForegroundColor Yellow

# Download best quality available (up to 1080p for stability)
# Assumes yt-dlp is installed and in PATH
& yt-dlp -f "bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best" `
    -o "$TempFile" `
    "https://www.youtube.com/watch?v=$YoutubeKey"

if ($LASTEXITCODE -ne 0) {
    Write-Error "[Ingestion] Failed to download video from YouTube."
    exit 1
}

Write-Host "[Ingestion] Stage 2: Starting multi-bitrate HLS transcoding" -ForegroundColor Yellow

# Run the transcoding script
& powershell.exe -ExecutionPolicy Bypass -File "$TranscodeScript" -InputFile "$TempFile" -OutputDir "$OutputDir"

if ($LASTEXITCODE -ne 0) {
    Write-Error "[Ingestion] Failed to transcode video."
    exit 1
}

# Cleanup the temporary download
Write-Host "[Ingestion] Cleaning up temp files" -ForegroundColor DarkGray
if (Test-Path $TempFile) {
    Remove-Item $TempFile -Force
}

Write-Host "[Ingestion] Pipeline complete. Signal Ready." -ForegroundColor Green
exit 0
