#!/bin/bash

# CEX Deployment Script
# Usage: ./deploy.sh [dev|prod] [action]

set -e

ENV=${1:-dev}
ACTION=${2:-up}

echo "========================================="
echo "CEX Platform Deployment Script"
echo "Environment: $ENV"
echo "Action: $ACTION"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    print_status "Docker and Docker Compose are installed"
}

# Create necessary directories
create_directories() {
    print_status "Creating necessary directories..."

    mkdir -p logs/{mysql,redis,rocketmq,nacos}
    mkdir -p data/{mysql,redis,rocketmq}
    mkdir -p backups
    mkdir -p ssl

    print_status "Directories created successfully"
}

# Copy environment file if not exists
setup_env() {
    if [ ! -f .env ]; then
        print_status "Creating .env file from template..."
        cp .env.example .env
        print_warning "Please update .env file with your configuration"
    fi
}

# Generate SSL certificates
generate_ssl() {
    if [ ! -f ssl/cert.pem ] || [ ! -f ssl/key.pem ]; then
        print_status "Generating self-signed SSL certificates..."
        mkdir -p ssl
        openssl req -x509 -newkey rsa:4096 -keyout ssl/key.pem -out ssl/cert.pem -days 365 -nodes -subj "/C=CN/ST=Beijing/L=Beijing/O=CEX/CN=localhost"
        print_status "SSL certificates generated"
    fi
}

# Setup database replication
setup_replication() {
    print_status "Setting up database replication..."

    # Wait for MySQL to be ready
    sleep 30

    # Get master status
    MASTER_STATUS=$(docker exec mysql-master mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW MASTER STATUS\G" | grep File | awk '{print $2}')
    MASTER_POS=$(docker exec mysql-master mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW MASTER STATUS\G" | grep Position | awk '{print $2}')

    # Configure slave
    docker exec mysql-slave mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e \
        "CHANGE MASTER TO MASTER_HOST='mysql-master', MASTER_USER='${MYSQL_REPLICATION_USER}', MASTER_PASSWORD='${MYSQL_REPLICATION_PASSWORD}', MASTER_LOG_FILE='${MASTER_STATUS}', MASTER_LOG_POS=${MASTER_POS};"

    docker exec mysql-slave mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "START SLAVE;"

    print_status "Database replication configured"
}

# Setup Redis cluster
setup_redis_cluster() {
    print_status "Setting up Redis cluster..."

    # Wait for Redis to be ready
    sleep 10

    # Create cluster
    docker exec redis-1 redis-cli --cluster create \
        redis-1:6379 redis-2:6380 redis-3:6381 \
        --cluster-replicas 0 --cluster-yes

    print_status "Redis cluster configured"
}

# Health check
health_check() {
    print_status "Running health checks..."

    # Check if all services are running
    SERVICES=("mysql-master" "mysql-slave" "redis-1" "redis-2" "redis-3" "rocketmq-nameserver" "rocketmq-broker-a" "rocketmq-broker-b" "nacos-1" "nacos-2" "nacos-3")

    for service in "${SERVICES[@]}"; do
        if docker ps | grep -q "$service"; then
            print_status "$service is running"
        else
            print_error "$service is not running"
        fi
    done

    # Check application services
    APP_SERVICES=("gateway-service" "user-service" "trade-service" "wallet-service" "market-service" "match-service" "finance-service" "risk-service" "notify-service" "frontend")

    for service in "${APP_SERVICES[@]}"; do
        if docker ps | grep -q "$service"; then
            print_status "$service is running"
        else
            print_warning "$service is not running"
        fi
    done
}

# Deploy
deploy() {
    print_status "Starting deployment..."

    # Stop existing services
    docker-compose -f docker-compose.${ENV}.yml down

    # Build and start services
    docker-compose -f docker-compose.${ENV}.yml build
    docker-compose -f docker-compose.${ENV}.yml up -d

    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 60

    # Setup additional configurations
    if [ "$ENV" = "prod" ]; then
        setup_replication
        setup_redis_cluster
    fi

    # Health check
    health_check

    print_status "Deployment completed successfully!"
}

# Stop services
stop() {
    print_status "Stopping services..."
    docker-compose -f docker-compose.${ENV}.yml down
    print_status "Services stopped"
}

# Restart services
restart() {
    print_status "Restarting services..."
    docker-compose -f docker-compose.${ENV}.yml restart
    print_status "Services restarted"
}

# View logs
logs() {
    if [ -z "$3" ]; then
        docker-compose -f docker-compose.${ENV}.yml logs -f
    else
        docker-compose -f docker-compose.${ENV}.yml logs -f "$3"
    fi
}

# Backup database
backup() {
    print_status "Creating database backup..."

    BACKUP_DIR="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"

    # Backup MySQL
    docker exec mysql-master mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} --all-databases > "$BACKUP_DIR/mysql_backup.sql"

    # Backup Redis
    docker exec redis-1 redis-cli --rdb "$BACKUP_DIR/redis_backup.rdb"

    # Backup configurations
    cp -r scripts "$BACKUP_DIR/"
    cp .env "$BACKUP_DIR/"

    print_status "Backup created at $BACKUP_DIR"
}

# Restore database
restore() {
    if [ -z "$3" ]; then
        print_error "Please provide backup directory"
        exit 1
    fi

    BACKUP_DIR="$3"

    if [ ! -d "$BACKUP_DIR" ]; then
        print_error "Backup directory not found: $BACKUP_DIR"
        exit 1
    fi

    print_status "Restoring from backup: $BACKUP_DIR"

    # Restore MySQL
    docker exec -i mysql-master mysql -uroot -p${MYSQL_ROOT_PASSWORD} < "$BACKUP_DIR/mysql_backup.sql"

    # Restore Redis (this is a simplified example)
    docker exec redis-1 redis-cli FLUSHALL
    docker exec redis-1 redis-cli --rdb "$BACKUP_DIR/redis_backup.rdb"

    print_status "Restore completed"
}

# Clean up
cleanup() {
    print_status "Cleaning up unused resources..."

    # Remove stopped containers
    docker container prune -f

    # Remove unused images
    docker image prune -f

    # Remove unused networks
    docker network prune -f

    # Remove unused volumes
    docker volume prune -f

    print_status "Cleanup completed"
}

# Show help
show_help() {
    echo "Usage: $0 [ENV] [ACTION] [OPTIONS]"
    echo ""
    echo "Environment:"
    echo "  dev        Development environment"
    echo "  prod       Production environment"
    echo ""
    echo "Actions:"
    echo "  up         Deploy services (default)"
    echo "  down       Stop services"
    echo "  restart    Restart services"
    echo "  logs       View logs"
    echo "  backup     Create backup"
    echo "  restore    Restore from backup"
    echo "  health     Health check"
    echo "  cleanup    Clean up unused resources"
    echo "  help       Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 dev up              # Deploy development environment"
    echo "  $0 prod up             # Deploy production environment"
    echo "  $0 dev logs user-service  # View logs for user-service"
    echo "  $0 prod backup          # Create backup"
    echo "  $0 prod restore /path/to/backup  # Restore from backup"
}

# Main script
main() {
    check_docker
    create_directories
    setup_env

    if [ "$ENV" = "prod" ]; then
        generate_ssl
    fi

    case "$ACTION" in
        up|deploy)
            deploy
            ;;
        down|stop)
            stop
            ;;
        restart)
            restart
            ;;
        logs)
            logs
            ;;
        backup)
            backup
            ;;
        restore)
            restore
            ;;
        health)
            health_check
            ;;
        cleanup)
            cleanup
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown action: $ACTION"
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@"