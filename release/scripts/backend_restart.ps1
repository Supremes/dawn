# 获取当前脚本所在目录的上两级目录作为项目根目录
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_PATH = (Get-Item "$ScriptDir\..\..").FullName
$RELEASE_PATH = "$PROJECT_PATH\release"

Write-Host "Project path: $PROJECT_PATH"
Write-Host "Release path: $RELEASE_PATH"

# 1. 进入 Maven 项目目录进行构建
Set-Location "$PROJECT_PATH\dawn-springboot"

# 执行 Maven 命令
cmd /c mvn clean package -T 1C -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Maven 构建成功" -ForegroundColor Green
} else {
    Write-Host "❌ Maven 构建失败" -ForegroundColor Red
    exit 1
}

# 2. 进入 Release 目录并拷贝 Jar 包
Set-Location $RELEASE_PATH
Copy-Item "$PROJECT_PATH\dawn-springboot\target\dawn-springboot-1.0.jar" . -Force

# 3. 执行 Docker Compose
# 注意：确保 docker-compose.yml 就在 release 目录下，或者你需要进一步指定文件位置
docker compose up -d --build nginx

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 成功重新创建并运行主程序 dawn" -ForegroundColor Green
} else {
    Write-Host "❌ Docker Compose 启动失败" -ForegroundColor Red
    exit 1
}