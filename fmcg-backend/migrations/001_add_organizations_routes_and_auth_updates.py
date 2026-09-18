"""
Database migration script for:
1. Adding Organization table
2. Adding Route table
3. Adding username to User table
4. Adding organization_id and route_id to appropriate tables

Run this script against your database after pulling the new code:
    python migrations/001_add_organizations_routes_and_auth_updates.py

For production databases, it's recommended to:
1. Backup your database first
2. Test migrations on a staging database first
3. Run during low-traffic hours
"""

from sqlalchemy import text, create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.exc import IntegrityError

# Import your settings
from app.core.config import settings
from app.models import Organization, Route, User

def run_migration():
    """Execute all migration steps."""
    engine = create_engine(settings.DATABASE_URL)
    Session = sessionmaker(bind=engine)
    db = Session()

    try:
        print("Starting migration...")

        # Step 1: Create Organization table if it doesn't exist
        print("Creating Organization table...")
        with engine.begin() as conn:
            conn.execute(text("""
                CREATE TABLE IF NOT EXISTS organizations (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(120) UNIQUE NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """))

        # Step 2: Create Route table if it doesn't exist
        print("Creating Route table...")
        with engine.begin() as conn:
            conn.execute(text("""
                CREATE TABLE IF NOT EXISTS routes (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(160) NOT NULL,
                    organization_id INTEGER NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
                    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """))
            conn.execute(text("CREATE INDEX IF NOT EXISTS ix_routes_organization_id ON routes(organization_id)"))

        # Step 3: Add columns to users table if they don't exist
        print("Updating users table...")
        with engine.begin() as conn:
            # Check if username column exists
            result = conn.execute(text("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name='users' AND column_name='username'
            """))
            if not result.first():
                conn.execute(text("""
                    ALTER TABLE users ADD COLUMN username VARCHAR(120) UNIQUE
                """))
                print("  - Added username column")

            # Check if organization_id column exists
            result = conn.execute(text("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name='users' AND column_name='organization_id'
            """))
            if not result.first():
                conn.execute(text("""
                    ALTER TABLE users ADD COLUMN organization_id INTEGER
                    REFERENCES organizations(id) ON DELETE SET NULL
                """))
                print("  - Added organization_id column")
                conn.execute(text("CREATE INDEX IF NOT EXISTS ix_users_organization_id ON users(organization_id)"))

        # Step 4: Add columns to stores table if they don't exist
        print("Updating stores table...")
        with engine.begin() as conn:
            # Check if route_id column exists
            result = conn.execute(text("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name='stores' AND column_name='route_id'
            """))
            if not result.first():
                conn.execute(text("""
                    ALTER TABLE stores ADD COLUMN route_id INTEGER
                    REFERENCES routes(id) ON DELETE SET NULL
                """))
                print("  - Added route_id column")
                conn.execute(text("CREATE INDEX IF NOT EXISTS ix_stores_route_id ON stores(route_id)"))

            # Check if organization_id column exists
            result = conn.execute(text("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name='stores' AND column_name='organization_id'
            """))
            if not result.first():
                conn.execute(text("""
                    ALTER TABLE stores ADD COLUMN organization_id INTEGER
                    REFERENCES organizations(id) ON DELETE CASCADE
                """))
                print("  - Added organization_id column")
                conn.execute(text("CREATE INDEX IF NOT EXISTS ix_stores_organization_id ON stores(organization_id)"))

        # Step 5: Create default organizations
        print("Creating default organizations...")
        result = db.query(Organization).filter_by(name="FMCG").first()
        if not result:
            fmcg = Organization(name="FMCG")
            db.add(fmcg)
            db.commit()
            print("  - Created FMCG organization")

        result = db.query(Organization).filter_by(name="NTC").first()
        if not result:
            ntc = Organization(name="NTC")
            db.add(ntc)
            db.commit()
            print("  - Created NTC organization")

        # Step 6: Set default usernames for existing users without one
        print("Setting default usernames for existing users...")
        fmcg_org = db.query(Organization).filter_by(name="FMCG").first()
        users_without_username = db.query(User).filter(User.username == None).all()

        for i, user in enumerate(users_without_username):
            # Create a default username from email or fallback
            if "@" in user.email:
                username = user.email.split("@")[0]
            else:
                username = f"user_{user.id}"

            # Ensure uniqueness
            counter = 0
            base_username = username
            while db.query(User).filter_by(username=username).first():
                counter += 1
                username = f"{base_username}_{counter}"

            user.username = username
            if user.organization_id is None:
                user.organization_id = fmcg_org.id if fmcg_org else None

        db.commit()
        print(f"  - Updated {len(users_without_username)} users")

        print("\n✅ Migration completed successfully!")
        print("\nNext steps:")
        print("1. Update your users' passwords if needed")
        print("2. Test login with the new username field")
        print("3. Verify all organizations and routes are set correctly")

    except Exception as e:
        print(f"\n❌ Migration failed: {e}")
        db.rollback()
        raise
    finally:
        db.close()

if __name__ == "__main__":
    run_migration()
