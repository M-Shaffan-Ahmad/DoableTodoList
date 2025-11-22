[Setup]
AppName=Doable Todo List
AppVersion=1.0.0
AppPublisher=Doable
AppPublisherURL=https://github.com/doable/doable-todo
AppSupportURL=https://github.com/doable/doable-todo
AppUpdatesURL=https://github.com/doable/doable-todo
DefaultDirName={userappdata}\Doable
DefaultGroupName=Doable
OutputDir=.\dist
OutputBaseFilename=Doable-1.0.0-Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
UninstallDisplayIcon={app}\Doable.ico
LicenseFile=.\LICENSE.txt
SetupIconFile=.\src\main\resources\app_icon.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked and iscustomtask; OnlyBelowVersion: 6.1,1

[Files]
Source: "target\doable-todo-1.0-SNAPSHOT.jar"; DestDir: "{app}"; Flags: ignoreversion; DestName: "Doable.jar"
Source: "src\main\resources\app_icon.png"; DestDir: "{app}"; Flags: ignoreversion
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "LICENSE.txt"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\Doable Todo List"; Filename: "{app}\Doable.jar"; Parameters: ""; WorkingDir: "{app}"; IconFileName: "{app}\app_icon.png"
Name: "{commondesktop}\Doable Todo List"; Filename: "{app}\Doable.jar"; Parameters: ""; WorkingDir: "{app}"; IconFileName: "{app}\app_icon.png"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\Doable Todo List"; Filename: "{app}\Doable.jar"; Parameters: ""; WorkingDir: "{app}"; IconFileName: "{app}\app_icon.png"; Tasks: quicklaunchicon

[Run]
Filename: "{code:GetJavaPath}\bin\java.exe"; Parameters: "-jar ""{app}\Doable.jar"""; Flags: nowait postinstall skipifsilent; Description: "Launch Doable Todo List"

[Code]
function GetJavaPath(Param: String): String;
var
  RegValue: String;
begin
  Result := '';
  
  { Try to find Java in registry }
  if RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', RegValue) then
  begin
    if RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\JavaSoft\Java Runtime Environment\' + RegValue, 'JavaHome', Result) then
      Exit;
  end;
  
  { Fallback: try to find java.exe in PATH }
  if FileExists('C:\Program Files\Java\jre1.8.0_281\bin\java.exe') then
    Result := 'C:\Program Files\Java\jre1.8.0_281'
  else if FileExists('C:\Program Files\OpenJDK\jdk-11\bin\java.exe') then
    Result := 'C:\Program Files\OpenJDK\jdk-11'
  else if FileExists('C:\Program Files (x86)\Java\jre1.8.0_281\bin\java.exe') then
    Result := 'C:\Program Files (x86)\Java\jre1.8.0_281'
  else
  begin
    MsgBox('Java is not installed. Please install Java 11 or higher from https://adoptium.net/', mbError, MB_OK);
    Result := '';
  end;
end;
