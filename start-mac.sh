#!/bin/bash
# ===================================================================
#  校园知乎 问答社区 — macOS 一键启动脚本
#  功能：环境检查与自动安装 → 数据库初始化 → 端口清理 → 启动全部服务
# ===================================================================

set -o pipefail
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; CYAN='\033[0;36m'; NC='\033[0m'; BOLD='\033[1m'
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

print_banner() {
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${BOLD}          校 园 知 乎  问 答 社 区                      ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}║${NC}          一键环境检查 & 启动脚本 (macOS)               ${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

check_and_install() {
    local name="$1" check_cmd="$2" install_cmd="$3" version_cmd="$4"
    echo -ne "  🔍 检查 ${BOLD}${name}${NC} ... "
    if eval "$check_cmd" &>/dev/null; then
        local ver=$(eval "$version_cmd" 2>&1 | head -1)
        echo -e "${GREEN}✅ 已安装${NC} ($ver)"
        return 0
    else
        echo -e "${YELLOW}❌ 未安装，正在自动安装...${NC}"
        echo -e "     运行: ${CYAN}${install_cmd}${NC}"
        eval "$install_cmd"
        if [ $? -eq 0 ]; then
            echo -e "     ${GREEN}✅ ${name} 安装成功${NC}"
            return 0
        else
            echo -e "     ${RED}❌ ${name} 安装失败，请手动安装${NC}"
            return 1
        fi
    fi
}

kill_port() {
    local port=$1
    local pids=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pids" ]; then
        echo -e "  ⚡ 端口 ${BOLD}${port}${NC} 被占用 (PID: $pids)，正在释放..."
        echo "$pids" | xargs kill -9 2>/dev/null
        sleep 1
        echo -e "     ${GREEN}✅ 端口 ${port} 已释放${NC}"
    else
        echo -e "  ✅ 端口 ${BOLD}${port}${NC} 可用"
    fi
}

# ======================== 开始 ========================
print_banner

# ============ 第一步：Homebrew ============
echo -e "${CYAN}━━━━━ 第 1 步：检查包管理器 Homebrew ━━━━━${NC}"
if ! command -v brew &>/dev/null; then
    echo -e "  ${YELLOW}❌ Homebrew 未安装，正在安装...${NC}"
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    # Apple Silicon
    [ -f /opt/homebrew/bin/brew ] && eval "$(/opt/homebrew/bin/brew shellenv)"
    # Intel
    [ -f /usr/local/bin/brew ] && eval "$(/usr/local/bin/brew shellenv)"
else
    echo -e "  ${GREEN}✅ Homebrew 已安装${NC} ($(brew --version | head -1))"
fi
echo ""

# ============ 第二步：环境依赖检查 ============
echo -e "${CYAN}━━━━━ 第 2 步：检查并安装环境依赖 ━━━━━${NC}"
INSTALL_FAILED=0

check_and_install "Java 17" \
    "java -version 2>&1 | grep -q '17'" \
    "brew install openjdk@17 && sudo ln -sfn $(brew --prefix openjdk@17)/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk" \
    "java -version 2>&1 | head -1" || INSTALL_FAILED=1

# 设置 JAVA_HOME
if [ -d "$(brew --prefix openjdk@17 2>/dev/null)/libexec/openjdk.jdk" ]; then
    export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

check_and_install "Maven" \
    "command -v mvn" \
    "brew install maven" \
    "mvn -version 2>&1 | head -1" || INSTALL_FAILED=1

check_and_install "Node.js" \
    "command -v node" \
    "brew install node" \
    "node -v" || INSTALL_FAILED=1

check_and_install "pnpm" \
    "command -v pnpm" \
    "npm install -g pnpm" \
    "pnpm -v" || INSTALL_FAILED=1

check_and_install "MySQL" \
    "command -v mysql" \
    "brew install mysql && brew services start mysql" \
    "mysql --version" || INSTALL_FAILED=1

if [ $INSTALL_FAILED -eq 1 ]; then
    echo -e "\n  ${RED}⚠️ 部分依赖安装失败，请手动处理后重新运行脚本${NC}"
    exit 1
fi
echo ""

# ============ 第三步：数据库初始化 ============
echo -e "${CYAN}━━━━━ 第 3 步：检查并初始化数据库 ━━━━━${NC}"

DB_USER="root"
DB_PASS="ab123168"
DB_NAME="campus_zhihu"
INIT_SQL="$BASE_DIR/backend/sql/init.sql"
DATA_SQL="$BASE_DIR/backend/sql/data.sql"

# 检查数据库是否存在
DB_EXISTS=$(mysql -u"$DB_USER" -p"$DB_PASS" -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep "$DB_NAME")

if [ -z "$DB_EXISTS" ]; then
    echo -e "  📦 数据库 ${BOLD}${DB_NAME}${NC} 不存在，正在创建并导入数据..."
    if [ -f "$INIT_SQL" ]; then
        mysql -u"$DB_USER" -p"$DB_PASS" < "$INIT_SQL" 2>/dev/null
        if [ $? -eq 0 ]; then
            echo -e "     ${GREEN}✅ 表结构已创建${NC} (init.sql)"
        else
            echo -e "     ${RED}❌ init.sql 导入失败，请检查 MySQL 连接 (用户:$DB_USER 密码:$DB_PASS)${NC}"
            echo -e "     ${YELLOW}提示: 如果密码不是 root，请修改 application.yml 和本脚本${NC}"
            exit 1
        fi
    fi
    if [ -f "$DATA_SQL" ]; then
        mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$DATA_SQL" 2>/dev/null
        if [ $? -eq 0 ]; then
            echo -e "     ${GREEN}✅ 测试数据已导入${NC} (data.sql)"
        else
            echo -e "     ${YELLOW}⚠️ data.sql 导入失败（可忽略，不影响启动）${NC}"
        fi
    fi
else
    echo -e "  ${GREEN}✅ 数据库 ${BOLD}${DB_NAME}${NC}${GREEN} 已存在，跳过初始化${NC}"
fi
echo ""

# ============ 第四步：清理端口 ============
echo -e "${CYAN}━━━━━ 第 4 步：检查并释放端口 ━━━━━${NC}"
kill_port 8080   # 后端
kill_port 5173   # 管理后台
echo ""

# ============ 第五步：安装前端依赖 ============
echo -e "${CYAN}━━━━━ 第 5 步：检查前端依赖 ━━━━━${NC}"

if [ ! -d "$BASE_DIR/admin/node_modules" ]; then
    echo -e "  📦 管理后台：安装 npm 依赖..."
    (cd "$BASE_DIR/admin" && pnpm install 2>&1 | tail -3)
    echo -e "     ${GREEN}✅ 管理后台依赖安装完成${NC}"
else
    echo -e "  ${GREEN}✅ 管理后台依赖已就绪${NC}"
fi
echo ""

# ============ 第六步：启动所有服务 ============
echo -e "${CYAN}━━━━━ 第 6 步：启动所有服务 ━━━━━${NC}"

# 启动后端
echo -e "  🚀 启动后端 (Spring Boot)..."
(cd "$BASE_DIR/backend/campus-zhihu" && ./mvnw spring-boot:run 2>&1 | tee "$LOG_DIR/backend.log" | while IFS= read -r line; do
    if echo "$line" | grep -qE '\[ERROR\]| ERROR |BUILD FAILURE|Compilation failure'; then
        echo -e "  ${RED}[后端错误] $line${NC}"
    fi
    if echo "$line" | grep -q "Started.*in.*seconds"; then
        echo -e "  ${GREEN}[后端] ✅ 启动成功！${NC}"
    fi
done) &
BACKEND_PID=$!
echo -e "     PID: ${BOLD}${BACKEND_PID}${NC} → 日志: logs/backend.log"

# 等待后端启动
echo -e "  ⏳ 等待后端启动 (最多60秒)..."
for i in $(seq 1 60); do
    if curl -s http://localhost:8080/api/auth/current >/dev/null 2>&1; then
        echo -e "  ${GREEN}✅ 后端已就绪 (${i}秒)${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "  ${YELLOW}⚠️ 后端启动超时，请检查 logs/backend.log${NC}"
    fi
    sleep 1
done

# 启动管理后台
echo -e "  🚀 启动管理后台 (Vue3 + Element Plus)..."
(cd "$BASE_DIR/admin" && pnpm run dev 2>&1 | tee "$LOG_DIR/admin.log" | while IFS= read -r line; do
    if echo "$line" | grep -qE 'ERROR|error:|ERR!|FATAL|failed to compile'; then
        echo -e "  ${RED}[管理后台错误] $line${NC}"
    fi
    if echo "$line" | grep -q "Local:"; then
        echo -e "  ${GREEN}[管理后台] ✅ 启动成功！${NC}"
    fi
done) &
ADMIN_PID=$!
echo -e "     PID: ${BOLD}${ADMIN_PID}${NC} → 日志: logs/admin.log"

sleep 3

# ============ 第七步：输出信息 ============
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${GREEN}${BOLD}            🎉 所有服务已启动！                          ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╠══════════════════════════════════════════════════════════╣${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  📌 ${BOLD}服务地址:${NC}                                           ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}     后端 API     → ${CYAN}http://localhost:8080${NC}                 ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}     API 文档     → ${CYAN}http://localhost:8080/doc.html${NC}        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}     管理后台     → ${CYAN}http://localhost:5173${NC}                 ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}     Android客户端 → 请用 Android Studio 打开 android/      ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}╠══════════════════════════════════════════════════════════╣${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  📌 ${BOLD}测试账号:${NC}                                           ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  ┌──────────┬──────────────┬──────────────┐        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  │  ${BOLD}角色${NC}    │  ${BOLD}用户名${NC}      │  ${BOLD}密码${NC}        │        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  ├──────────┼──────────────┼──────────────┤        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  │  管理员  │  admin        │  admin123    │        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  │  教师    │  teacher01    │  123456      │        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  │  学生    │  student01    │  123456      │        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  └──────────┴──────────────┴──────────────┘        ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}╠══════════════════════════════════════════════════════════╣${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  📌 ${BOLD}日志目录:${NC} logs/                                     ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}     backend.log  — 后端日志                              ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}     admin.log    — 管理后台日志                          ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}  ${YELLOW}按 Ctrl+C 停止所有服务${NC}                                  ${BLUE}║${NC}"
echo -e "${BLUE}║${NC}                                                          ${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# 捕获 Ctrl+C 信号，优雅退出
cleanup() {
    echo ""
    echo -e "${YELLOW}🛑 正在停止所有服务...${NC}"
    kill $BACKEND_PID 2>/dev/null
    kill $ADMIN_PID 2>/dev/null
    # 确保端口释放
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    lsof -ti:5173 | xargs kill -9 2>/dev/null
    echo -e "${GREEN}✅ 所有服务已停止${NC}"
    exit 0
}
trap cleanup SIGINT SIGTERM

# 保持脚本运行，实时显示日志错误
echo -e "${CYAN}📋 实时监控日志 (仅显示错误)...${NC}"
echo ""
tail -f "$LOG_DIR/backend.log" "$LOG_DIR/admin.log" 2>/dev/null | while IFS= read -r line; do
    if echo "$line" | grep -qE '\[ERROR\]| ERROR | WARN |BUILD FAILURE|ERR!|failed to compile'; then
        timestamp=$(date '+%H:%M:%S')
        echo -e "${RED}[$timestamp] $line${NC}"
    fi
done
