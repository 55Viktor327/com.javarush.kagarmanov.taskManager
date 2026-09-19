#!/usr/bin/env bash
set -uo pipefail

BASE=http://localhost:8080/api

# Цвета
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Счётчики
PASS=0
FAIL=0

# Функция проверки
check() {
  local desc="$1"
  local expected="$2"
  local actual="$3"
  if [ "$expected" = "$actual" ]; then
    echo -e "${GREEN}✅ PASS${NC} [$actual] $desc"
    PASS=$((PASS+1))
  else
    echo -e "${RED}❌ FAIL${NC} [ожидалось $expected, получено $actual] $desc"
    FAIL=$((FAIL+1))
  fi
}

# Функция запроса — возвращает только HTTP-код
req() {
  local method="$1" url="$2" token="$3" body="${4:-}"
  if [ -n "$body" ]; then
    curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
      ${token:+-H "Authorization: Bearer $token"} \
      -H "Content-Type: application/json" \
      -d "$body"
  else
    curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
      ${token:+-H "Authorization: Bearer $token"}
  fi
}

echo "════════════════════════════════════════"
echo "  ТЕСТИРОВАНИЕ API TASK MANAGER"
echo "════════════════════════════════════════"

# ─── 1. Получить токены ───
echo ""
echo "▶ Получение токенов..."

TOKEN_JOHN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"Secret123@"}' | jq -r '.token')

TOKEN_IVAN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ivan_ivanov","password":"Pass123@"}' | jq -r '.token')

TOKEN_JANE=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","password":"Secret123@"}' | jq -r '.token')

echo "john (SUPER_ADMIN): ${#TOKEN_JOHN} символов"
echo "ivan (USER):        ${#TOKEN_IVAN} символов"
echo "jane (USER):        ${#TOKEN_JANE} символов"

# ─── 2. Auth ───
echo ""
echo "▶ AUTH"

check "POST /auth/login без тела → 400" \
  400 "$(req POST $BASE/auth/login "" '{}')"

check "POST /auth/register дубликат → 409" \
  409 "$(req POST $BASE/auth/register "" '{"userName":"john","email":"x@x.com","password":"Secret123@"}')"

# ─── 3. Без токена ───
echo ""
echo "▶ БЕЗ ТОКЕНА (ожидаем 401)"

check "GET /users/1 → 401" \
  401 "$(req GET $BASE/users/1 "")"

check "GET /tasks → 401" \
  401 "$(req GET $BASE/tasks "")"

check "POST /tasks/create → 401" \
  401 "$(req POST $BASE/tasks/create "" '{"title":"x","description":"x","deadline":"2026-12-31T23:59:59"}')"

# ─── 4. GUEST (jane, если ещё GUEST) ───
echo ""
echo "▶ GUEST (jane)"

check "GET /users → 200 (видит всех!)" \
  200 "$(req GET $BASE/users "$TOKEN_JANE")"

check "GET /tasks → 200" \
  200 "$(req GET $BASE/tasks "$TOKEN_JANE")"

check "POST /tasks/create → 403 (GUEST не создаёт)" \
  403 "$(req POST $BASE/tasks/create "$TOKEN_JANE" '{"title":"x","description":"x","deadline":"2026-12-31T23:59:59"}')"

# ─── 5. USER (ivan) ───
echo ""
echo "▶ USER (ivan)"

check "GET /users → 200" \
  200 "$(req GET $BASE/users "$TOKEN_IVAN")"

check "PUT /users/2/role → 403 (USER не меняет роли)" \
  403 "$(req PUT $BASE/users/2/role "$TOKEN_IVAN" '{"role":"ADMIN"}')"

check "PUT /users/3/role → 403 (USER не меняет чужую роль)" \
  403 "$(req PUT $BASE/users/3/role "$TOKEN_IVAN" '{"role":"ADMIN"}')"

check "DELETE /users/3 → 403 (USER не удаляет других)" \
  403 "$(req DELETE $BASE/users/3 "$TOKEN_IVAN")"

check "GET /tasks/deleted → 403 (USER не видит удалённые)" \
  403 "$(req GET $BASE/tasks/deleted "$TOKEN_IVAN")"

check "GET /users/deleted → 403 (USER не видит удалённых)" \
  403 "$(req GET $BASE/users/deleted "$TOKEN_IVAN")"

TITLE_IVAN="Задача USER $(date +%s)"
check "POST /tasks/create → 201 (USER)" \
  201 "$(req POST $BASE/tasks/create "$TOKEN_IVAN" "{\"title\":\"$TITLE_IVAN\",\"description\":\"test\",\"deadline\":\"2026-12-31T23:59:59\"}")"
  
check "GET /tasks/2 → 200" \
  200 "$(req GET $BASE/tasks/2 "$TOKEN_IVAN")"

check "PATCH /tasks/2/title → 200" \
  200 "$(req PATCH $BASE/tasks/2/title "$TOKEN_IVAN" '{"title":"Обновлено"}')"

check "POST /tasks/1/assignees/1 → 403 (USER не назначает)" \
  403 "$(req POST $BASE/tasks/1/assignees/1 "$TOKEN_IVAN")"

# ─── 6. SUPER_ADMIN (john) ───
echo ""
echo "▶ SUPER_ADMIN (john)"

check "GET /users → 200" \
  200 "$(req GET $BASE/users "$TOKEN_JOHN")"

check "GET /users/deleted → 200" \
  200 "$(req GET $BASE/users/deleted "$TOKEN_JOHN")"

check "GET /tasks/deleted → 200" \
  200 "$(req GET $BASE/tasks/deleted "$TOKEN_JOHN")"

check "PUT /users/2/role → 200 (SUPER_ADMIN меняет)" \
  200 "$(req PUT $BASE/users/2/role "$TOKEN_JOHN" '{"role":"USER"}')"

check "POST /tasks/1/assignees/2 → 200 (SUPER_ADMIN назначает)" \
  200 "$(req POST $BASE/tasks/1/assignees/2 "$TOKEN_JOHN")"
  
check "DELETE /tasks/1/assignees/2 → 200" \
    200 "$(req DELETE $BASE/tasks/1/assignees/2 "$TOKEN_JOHN")"

check "DELETE /tasks/1 → 200 (SUPER_ADMIN удаляет)" \
    200 "$(req DELETE $BASE/tasks/1 "$TOKEN_JOHN")"
  
  check "POST /tasks/1/restore → 200 (SUPER_ADMIN восстанавливает)" \
    200 "$(req POST $BASE/tasks/1/restore "$TOKEN_JOHN")"

# ─── Итог ───
echo ""
echo "════════════════════════════════════════"
echo -e "  ${GREEN}PASS: $PASS${NC}   ${RED}FAIL: $FAIL${NC}"
echo "════════════════════════════════════════"

if [ $FAIL -gt 0 ]; then
  exit 1
fi
