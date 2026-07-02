#!/bin/bash
# ============================================
# Backup automático de PostgreSQL hacia bastion
# Sistema Inteligente de Pasantías UCE
# Corre diariamente a las 2am via cron
# ============================================

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/backups/postgres"
LOG_FILE="/var/log/backup_postgres.log"
POSTGRES_HOST="postgres-db"
POSTGRES_USER="postgres"
POSTGRES_PASS="postgres"
BASTION_USER="ubuntu"
BASTION_IP="${BASTION_IP:-10.0.1.166}"
RETENTION_DAYS=7

DATABASES=(
  "auth_db"
  "internship_db"
  "user_db"
  "linkage_db"
  "hours_db"
  "evaluation_db"
  "notification_db"
  "document_db"
  "report_db"
)

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

mkdir -p $BACKUP_DIR
log "=== Iniciando backup de PostgreSQL ==="

SUCCESS=0
FAILED=0

for DB in "${DATABASES[@]}"; do
  BACKUP_FILE="$BACKUP_DIR/${DB}_${DATE}.sql.gz"
  
  log "Respaldando base de datos: $DB"
  
  docker exec postgres-db pg_dump \
    -U $POSTGRES_USER \
    -d $DB \
    --no-password 2>/dev/null | gzip > $BACKUP_FILE
  
  if [ $? -eq 0 ] && [ -s $BACKUP_FILE ]; then
    SIZE=$(du -sh $BACKUP_FILE | cut -f1)
    log "✅ $DB respaldada exitosamente ($SIZE)"
    SUCCESS=$((SUCCESS + 1))
  else
    log "❌ Error respaldando $DB"
    rm -f $BACKUP_FILE
    FAILED=$((FAILED + 1))
  fi
done

log "Eliminando backups con más de $RETENTION_DAYS días..."
find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete
log "Limpieza completada"

log "=== Backup completado: $SUCCESS exitosos, $FAILED fallidos ==="

if [ $FAILED -gt 0 ]; then
  exit 1
fi
exit 0
