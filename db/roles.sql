-- Log into psql and then:
CREATE USER calendly WITH PASSWORD 'calendly@911#';
GRANT ALL PRIVILEGES ON DATABASE calendly TO calendly;


grant USAGE on SCHEMA public to calendly;
grant USAGE on SCHEMA stg to calendly;

grant USAGE on all sequences in schema public to calendly;
grant USAGE on all sequences in schema stg to calendly;

alter default privileges in schema public grant all privileges on tables to calendly;
alter default privileges in schema stg grant usage on sequences to calendly;
alter default privileges for role calendly in schema public grant all on functions to calendly;
alter default privileges for role calendly in schema stg grant all on functions to calendly;

grant all on SCHEMA stg to calendly;
grant all on SCHEMA public to calendly;
grant all on all tables in schema stg to calendly;
grant all on all tables in schema public to calendly;

grant all privileges on all tables in schema stg to calendly;
grant all privileges on all tables in schema public to calendly;
grant EXECUTE on all functions in schema stg to calendly;
grant EXECUTE on all functions in schema public to calendly;
