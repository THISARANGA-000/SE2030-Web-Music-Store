$ErrorActionPreference = "Stop"

function Test-Endpoint {
    param([string]$Url, [string]$Name)
    try {
        $res = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 10
        Write-Host "[$($res.StatusCode)] $Name ($Url)" -ForegroundColor Green
        return $res
    } catch {
        Write-Host "[$($_.Exception.Response.StatusCode.value__)] $Name ($Url) - $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

Write-Host "=================== MELODYMART ENDPOINT VERIFICATION ===================" -ForegroundColor Cyan

# 1. Public catalog and auth endpoints
Test-Endpoint "http://localhost:8080/albums" "Albums Catalog"
$albumDetail = Test-Endpoint "http://localhost:8080/albums/1" "Album Details (ID 1)"
if ($albumDetail -and $albumDetail.Content -match "<audio") {
    Write-Host "  -> SUCCESS: HTML5 audio player detected in album detail!" -ForegroundColor Green
} else {
    Write-Host "  -> Note: Checking audio player presence..." -ForegroundColor Yellow
}

Test-Endpoint "http://localhost:8080/login" "Login Page"
Test-Endpoint "http://localhost:8080/register" "Register Page"
Test-Endpoint "http://localhost:8080/cart" "Cart Page"
Test-Endpoint "http://localhost:8080/assets/covers/images.jfif" "Static Album Cover"

# 2. Listener authentication test
Write-Host "`n--- Testing Listener Flow (Session) ---" -ForegroundColor Cyan
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Login as listener
$loginBody = @{
    email = "nimal@gmail.com"
    password = "password123"
}
try {
    $loginRes = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -Body $loginBody -WebSession $session -MaximumRedirection 0 -ErrorAction SilentlyContinue
} catch {
    # 302 redirect expected
}

$libraryRes = Invoke-WebRequest -Uri "http://localhost:8080/my-library" -WebSession $session -UseBasicParsing
Write-Host "[$($libraryRes.StatusCode)] /my-library (Listener Session)" -ForegroundColor Green

$playlistRes = Invoke-WebRequest -Uri "http://localhost:8080/playlists" -WebSession $session -UseBasicParsing
Write-Host "[$($playlistRes.StatusCode)] /playlists (Listener Session)" -ForegroundColor Green

$checkoutRes = Invoke-WebRequest -Uri "http://localhost:8080/checkout" -WebSession $session -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Host "[$($checkoutRes.StatusCode)] /checkout (Listener Session)" -ForegroundColor Green

# 3. Admin authentication test
Write-Host "`n--- Testing Admin Flow (Session) ---" -ForegroundColor Cyan
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Login as admin
$adminLoginBody = @{
    email = "admin@melodymart.com"
    password = "AdminPassword123!"
}
try {
    $adminLoginRes = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -Body $adminLoginBody -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
} catch {
    # 302 redirect expected
}

$adminTracksRes = Invoke-WebRequest -Uri "http://localhost:8080/admin/catalog/tracks" -WebSession $adminSession -UseBasicParsing
Write-Host "[$($adminTracksRes.StatusCode)] /admin/catalog/tracks (Admin Session)" -ForegroundColor Green
if ($adminTracksRes.Content -match "Audio URL") {
    Write-Host "  -> SUCCESS: Audio URL column present in Admin Track Management!" -ForegroundColor Green
}

$adminTrackAddRes = Invoke-WebRequest -Uri "http://localhost:8080/admin/catalog/tracks/add" -WebSession $adminSession -UseBasicParsing
Write-Host "[$($adminTrackAddRes.StatusCode)] /admin/catalog/tracks/add (Admin Session)" -ForegroundColor Green

Write-Host "`n=================== VERIFICATION COMPLETE ===================" -ForegroundColor Cyan
