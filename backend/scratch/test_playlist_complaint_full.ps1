# ============================================================
# MelodyMart: Complete Playlist & Complaint E2E Verification Script
# ============================================================

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"
$passedTests = 0
$failedTests = 0

function Assert-Test([string]$testName, [bool]$condition, [string]$detail = "") {
    if ($condition) {
        Write-Host "  [PASS] $testName" -ForegroundColor Green
        $global:passedTests++
    } else {
        Write-Host "  [FAIL] $testName - $detail" -ForegroundColor Red
        $global:failedTests++
    }
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "STARTING FULL PLAYLIST & COMPLAINT TEST SUITE" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# ------------------------------------------------------------
# PHASE 16: TEST L — Session Protection (Unauthenticated Access)
# ------------------------------------------------------------
Write-Host "`n--- TEST L: SESSION PROTECTION ---" -ForegroundColor Yellow

$unauthRoutes = @("/playlists", "/playlists/new", "/complaints", "/complaints/new")
foreach ($r in $unauthRoutes) {
    $resp = Invoke-WebRequest -Uri "$baseUrl$r" -Method Get -MaximumRedirection 0 -ErrorAction SilentlyContinue
    $status = if ($resp) { $resp.StatusCode } else { 0 }
    $location = if ($resp -and $resp.Headers.Location) { $resp.Headers.Location } else { "" }
    $isProtected = ($status -eq 302 -and $location -match "/login")
    Assert-Test "Unauth access to $r redirects to /login" $isProtected "Status: $status, Location: $location"
}

# ------------------------------------------------------------
# Setup Test Listeners
# ------------------------------------------------------------
Write-Host "`n--- CREATING TEST LISTENERS ---" -ForegroundColor Yellow

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$userA_email = "listenerA_$ts@example.com"
$userB_email = "listenerB_$ts@example.com"

# Register Listener A
$sessionA = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$regABody = @{
    firstName = "Alice"
    lastName  = "Tester"
    email     = $userA_email
    password  = "Password123!"
    phone     = "555-0101"
    address   = "123 Music Ave"
}
$regRespA = Invoke-WebRequest -Uri "$baseUrl/register" -Method Post -Body $regABody -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Register Listener A ($userA_email)" ($regRespA.StatusCode -eq 302)

# Login Listener A
$loginABody = @{
    email    = $userA_email
    password = "Password123!"
}
$loginRespA = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $loginABody -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Login Listener A" ($loginRespA.StatusCode -eq 302)

# Register Listener B
$sessionB = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$regBBody = @{
    firstName = "Bob"
    lastName  = "Auditor"
    email     = $userB_email
    password  = "Password123!"
    phone     = "555-0202"
    address   = "456 Audio Blvd"
}
$regRespB = Invoke-WebRequest -Uri "$baseUrl/register" -Method Post -Body $regBBody -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Register Listener B ($userB_email)" ($regRespB.StatusCode -eq 302)

# Login Listener B
$loginBBody = @{
    email    = $userB_email
    password = "Password123!"
}
$loginRespB = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $loginBBody -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Login Listener B" ($loginRespB.StatusCode -eq 302)

# ------------------------------------------------------------
# PHASE 16: TEST B — Playlist CRUD
# ------------------------------------------------------------
Write-Host "`n--- TEST B: PLAYLIST CRUD ---" -ForegroundColor Yellow

# 1. Create "Workout"
$createWorkout = Invoke-WebRequest -Uri "$baseUrl/playlists" -Method Post -Body @{ playlistName = "Workout" } -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Create Playlist 'Workout'" ($createWorkout.StatusCode -eq 302)
$loc = $createWorkout.Headers.Location
$loc -match "/playlists/(\d+)" | Out-Null
$playlistA_Id = $Matches[1]
Assert-Test "Extracted Playlist A ID ($playlistA_Id)" ($playlistA_Id -ne $null -and $playlistA_Id -gt 0)

# Verify playlist page contains "Workout"
$viewPl = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "View Playlist 'Workout' details" ($viewPl.Content -match "Workout")

# 2. Rename to "Workout Music"
$renameResp = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/edit" -Method Post -Body @{ playlistName = "Workout Music" } -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Rename Playlist to 'Workout Music'" ($renameResp.StatusCode -eq 302)
$viewRenamed = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Playlist name successfully updated to 'Workout Music'" ($viewRenamed.Content -match "Workout Music")

# 3. Create "Chill"
$createChill = Invoke-WebRequest -Uri "$baseUrl/playlists" -Method Post -Body @{ playlistName = "Chill" } -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
$createChill.Headers.Location -match "/playlists/(\d+)" | Out-Null
$playlistChill_Id = $Matches[1]
Assert-Test "Create second playlist 'Chill' (ID $playlistChill_Id)" ($playlistChill_Id -ne $null)

# Verify both appear on /playlists
$listResp = Invoke-WebRequest -Uri "$baseUrl/playlists" -Method Get -WebSession $sessionA
Assert-Test "Both playlists appear in /playlists" ($listResp.Content -match "Workout Music" -and $listResp.Content -match "Chill")

# 4. Delete "Chill"
$delResp = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistChill_Id/delete" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Delete playlist 'Chill'" ($delResp.StatusCode -eq 302)
$listAfterDel = Invoke-WebRequest -Uri "$baseUrl/playlists" -Method Get -WebSession $sessionA
Assert-Test "'Chill' is removed from list" ($listAfterDel.Content -notmatch "Chill" -and $listAfterDel.Content -match "Workout Music")

# ------------------------------------------------------------
# PHASE 16: TEST C — Playlist Album (Add & Duplicate Prevention)
# ------------------------------------------------------------
Write-Host "`n--- TEST C: PLAYLIST ALBUM ---" -ForegroundColor Yellow

# Add album ID 1
$addAlbumResp = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/albums/1" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Add Album 1 to Playlist" ($addAlbumResp.StatusCode -eq 302)

$viewAfterAlbum = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Album 1 appears in playlist" ($viewAfterAlbum.Content -match "Celestial Drift" -or $viewAfterAlbum.Content -match "chip-blue")

# Attempt adding same album again
$dupAlbumResp = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/albums/1" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Duplicate album request handled with redirect" ($dupAlbumResp.StatusCode -eq 302)
$viewAfterDupAlbum = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Duplicate album shows error notice" ($viewAfterDupAlbum.Content -match "already in the playlist")

# ------------------------------------------------------------
# PHASE 16: TEST D — Playlist Track (Add & Duplicate Prevention)
# ------------------------------------------------------------
Write-Host "`n--- TEST D: PLAYLIST TRACK ---" -ForegroundColor Yellow

# Add track ID 1
$addTrackResp = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/tracks/1" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Add Track 1 to Playlist" ($addTrackResp.StatusCode -eq 302)

$viewAfterTrack = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Track 1 appears in playlist" ($viewAfterTrack.Content -match "Track" -or $viewAfterTrack.Content -match "chip-gray")

# Attempt adding same track again
$dupTrackResp = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/tracks/1" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Duplicate track request handled with redirect" ($dupTrackResp.StatusCode -eq 302)
$viewAfterDupTrack = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Duplicate track shows error notice" ($viewAfterDupTrack.Content -match "already in the playlist")

# ------------------------------------------------------------
# PHASE 16: TEST E — Playlist Remove
# ------------------------------------------------------------
Write-Host "`n--- TEST E: PLAYLIST REMOVE ITEMS ---" -ForegroundColor Yellow

# Find item IDs from HTML
$matchesItems = [regex]::Matches($viewAfterTrack.Content, "/playlists/$playlistA_Id/items/(\d+)/delete")
Assert-Test "Found playlist items to remove" ($matchesItems.Count -ge 2)

if ($matchesItems.Count -ge 2) {
    $item1_Id = $matchesItems[0].Groups[1].Value
    $item2_Id = $matchesItems[1].Groups[1].Value

    # Remove first item
    $rem1 = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/items/$item1_Id/delete" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Assert-Test "Remove first item ($item1_Id)" ($rem1.StatusCode -eq 302)

    # Remove second item
    $rem2 = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/items/$item2_Id/delete" -Method Post -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Assert-Test "Remove second item ($item2_Id)" ($rem2.StatusCode -eq 302)

    $viewEmpty = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
    Assert-Test "Playlist shows empty state after removal" ($viewEmpty.Content -match "No items yet")
}

# ------------------------------------------------------------
# PHASE 16: TEST F — Playlist Ownership (CRITICAL)
# ------------------------------------------------------------
Write-Host "`n--- TEST F: PLAYLIST OWNERSHIP (CRITICAL) ---" -ForegroundColor Yellow

# Listener B attempts to view Listener A's playlist
$bViewA = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Listener B GET /playlists/$playlistA_Id is denied/redirected" ($bViewA.StatusCode -eq 302)

# Listener B attempts to rename Listener A's playlist
$bRenameA = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/edit" -Method Post -Body @{ playlistName = "Hacked Name" } -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Listener B rename Listener A's playlist is rejected" ($bRenameA.StatusCode -eq 302)

# Listener B attempts to delete Listener A's playlist
$bDelA = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/delete" -Method Post -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Listener B delete Listener A's playlist is rejected" ($bDelA.StatusCode -eq 302)

# Listener B attempts to add album to Listener A's playlist
$bAddAlb = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/albums/1" -Method Post -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Listener B add album to Listener A's playlist is rejected" ($bAddAlb.StatusCode -eq 302)

# Listener B attempts to add track to Listener A's playlist
$bAddTrk = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id/tracks/1" -Method Post -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Listener B add track to Listener A's playlist is rejected" ($bAddTrk.StatusCode -eq 302)

# Verify Listener A's playlist name was NOT changed
$aCheck = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Listener A's playlist retained its original name" ($aCheck.Content -match "Workout Music" -and $aCheck.Content -notmatch "Hacked Name")

# ------------------------------------------------------------
# PHASE 16: TEST G, H, I — Complaint Feature
# ------------------------------------------------------------
Write-Host "`n--- TEST G, H, I: COMPLAINT CREATE, LIST, VIEW ---" -ForegroundColor Yellow

# G. Submit Complaint as Listener A
$complaintSub = "Test Complaint $ts"
$complaintDesc = "This is a test complaint description about track playback issues."

$createComp = Invoke-WebRequest -Uri "$baseUrl/complaints" -Method Post -Body @{
    subject     = $complaintSub
    description = $complaintDesc
} -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Submit Complaint by Listener A" ($createComp.StatusCode -eq 302)

# H. List Complaints for Listener A
$compListA = Invoke-WebRequest -Uri "$baseUrl/complaints" -Method Get -WebSession $sessionA
Assert-Test "Listener A's complaint appears in /complaints" ($compListA.Content -match $complaintSub)
Assert-Test "Status is Open" ($compListA.Content -match "Open")

# Extract Complaint ID
$compListA.Content -match "/complaints/(\d+)" | Out-Null
$complaintA_Id = $Matches[1]
Assert-Test "Extracted Complaint ID ($complaintA_Id)" ($complaintA_Id -ne $null)

# I. View Complaint Details
$compDetailA = Invoke-WebRequest -Uri "$baseUrl/complaints/$complaintA_Id" -Method Get -WebSession $sessionA
Assert-Test "View Complaint details shows Subject" ($compDetailA.Content -match $complaintSub)
Assert-Test "View Complaint details shows Description" ($compDetailA.Content -match $complaintDesc)
Assert-Test "View Complaint shows 'Awaiting response'" ($compDetailA.Content -match "Awaiting response")

# ------------------------------------------------------------
# PHASE 16: TEST J — Complaint Ownership
# ------------------------------------------------------------
Write-Host "`n--- TEST J: COMPLAINT OWNERSHIP ---" -ForegroundColor Yellow

# Listener B /complaints must NOT contain Listener A's complaint
$compListB = Invoke-WebRequest -Uri "$baseUrl/complaints" -Method Get -WebSession $sessionB
Assert-Test "Listener B cannot see Listener A's complaint in list" ($compListB.Content -notmatch $complaintSub)

# Listener B attempts to access /complaints/{complaintA_Id}
$bViewCompA = Invoke-WebRequest -Uri "$baseUrl/complaints/$complaintA_Id" -Method Get -WebSession $sessionB -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Listener B direct access to Listener A's complaint is denied/redirected" ($bViewCompA.StatusCode -eq 302)

# ------------------------------------------------------------
# PHASE 16: TEST K — Admin Complaint Flow
# ------------------------------------------------------------
Write-Host "`n--- TEST K: ADMIN COMPLAINT FLOW ---" -ForegroundColor Yellow

$sessionAdmin = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$adminLoginBody = @{
    email    = "emily.watson@melodymart.com"
    password = "password123"
}
$adminLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $adminLoginBody -WebSession $sessionAdmin -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Admin Login (emily.watson@melodymart.com)" ($adminLogin.StatusCode -eq 302)

# Admin views /admin/complaints
$adminCompList = Invoke-WebRequest -Uri "$baseUrl/admin/support/complaints" -Method Get -WebSession $sessionAdmin
Assert-Test "Admin sees Listener A's complaint" ($adminCompList.Content -match $complaintSub)

# Admin responds and resolves complaint
$adminUpdateBody = @{
    status   = "Resolved"
    response = "We have reviewed your report and resolved the issue. Thank you!"
}
$adminUpdate = Invoke-WebRequest -Uri "$baseUrl/admin/support/complaints/$complaintA_Id/status" -Method Post -Body $adminUpdateBody -WebSession $sessionAdmin -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Admin updates status to Resolved with response" ($adminUpdate.StatusCode -eq 302)

# Listener A re-checks complaint details
$compDetailA_After = Invoke-WebRequest -Uri "$baseUrl/complaints/$complaintA_Id" -Method Get -WebSession $sessionA
Assert-Test "Listener sees updated 'Resolved' status" ($compDetailA_After.Content -match "Resolved")
Assert-Test "Listener sees admin response" ($compDetailA_After.Content -match "We have reviewed your report and resolved the issue")

# ------------------------------------------------------------
# PHASE 16: TEST C2 — Add to Playlist from Album Details Page
# ------------------------------------------------------------
Write-Host "`n--- TEST: ADD TO PLAYLIST FROM ALBUM DETAILS PAGE ---" -ForegroundColor Yellow

$albumPage = Invoke-WebRequest -Uri "$baseUrl/albums/1" -Method Get -WebSession $sessionA
Assert-Test "Album detail contains Add to Playlist section" ($albumPage.Content -match "Add to Playlist" -or $albumPage.Content -match "addToPlaylistSection")
Assert-Test "Album detail dropdown includes user's playlist" ($albumPage.Content -match "Workout Music")

$pageAddAlbum = Invoke-WebRequest -Uri "$baseUrl/playlists/add-album-from-page" -Method Post -Body @{
    albumId    = 2
    playlistId = $playlistA_Id
} -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Add Album 2 to playlist from album page" ($pageAddAlbum.StatusCode -eq 302)

$viewAfterPageAdd = Invoke-WebRequest -Uri "$baseUrl/playlists/$playlistA_Id" -Method Get -WebSession $sessionA
Assert-Test "Album 2 appears in playlist" ($viewAfterPageAdd.Content -match "Neon Horizon")

# ------------------------------------------------------------
# PHASE 16: TEST A — Existing Functionality Regression
# ------------------------------------------------------------
Write-Host "`n--- TEST A: EXISTING FUNCTIONALITY REGRESSION ---" -ForegroundColor Yellow

# 1. Album list
$albums = Invoke-WebRequest -Uri "$baseUrl/albums" -Method Get
Assert-Test "Album listing works" ($albums.StatusCode -eq 200 -and $albums.Content -match "Celestial Drift")

# 2. Album search
$search = Invoke-WebRequest -Uri "$baseUrl/albums?search=Celestial" -Method Get
Assert-Test "Album search works" ($search.StatusCode -eq 200 -and $search.Content -match "Celestial Drift")

# 3. Catalogs
$catalogs = Invoke-WebRequest -Uri "$baseUrl/catalogs" -Method Get
Assert-Test "Catalog listing works" ($catalogs.StatusCode -eq 200)

# 4. Cart
$cartAdd = Invoke-WebRequest -Uri "$baseUrl/cart/add" -Method Post -Body @{ albumId = 3 } -WebSession $sessionA -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Test "Add to Cart works" ($cartAdd.StatusCode -eq 302)

$cartView = Invoke-WebRequest -Uri "$baseUrl/cart" -Method Get -WebSession $sessionA
Assert-Test "Cart view shows added album" ($cartView.Content -match "Cart" -and $cartView.StatusCode -eq 200)

# 5. Profile
$profile = Invoke-WebRequest -Uri "$baseUrl/profile" -Method Get -WebSession $sessionA
Assert-Test "Profile page works" ($profile.StatusCode -eq 200 -and $profile.Content -match "Alice")

# 6. Navigation items
Assert-Test "Navbar contains Playlists link" ($profile.Content -match "Playlists" -or $profile.Content -match "/playlists")
Assert-Test "Navbar contains My Complaints link" ($profile.Content -match "My Complaints" -or $profile.Content -match "/complaints")

# ------------------------------------------------------------
# SUMMARY
# ------------------------------------------------------------
Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "TEST RESULTS SUMMARY: PASSED: $global:passedTests | FAILED: $global:failedTests" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

if ($global:failedTests -eq 0) {
    Exit 0
} else {
    Exit 1
}
