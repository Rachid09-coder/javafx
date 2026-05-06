$jar = (Get-ChildItem -Path C:\Users\firas\.m2\repository\com\google\http-client\google-http-client\ -Recurse -Filter *.jar | Select-Object -First 1).FullName
[Reflection.Assembly]::LoadWithPartialName('System.IO.Compression.FileSystem') | Out-Null
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
$entry = $zip.GetEntry('META-INF/MANIFEST.MF')
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$manifest = $reader.ReadToEnd()
$reader.Close()
$zip.Dispose()
$manifest | Select-String 'Automatic-Module-Name'
