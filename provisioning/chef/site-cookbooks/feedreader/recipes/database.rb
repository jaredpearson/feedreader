
include_recipe 'database::postgresql'

postgresql_connection_info = {
	:host => "127.0.0.1",
	:port => node['postgresql']['config']['port'],
	:username => 'postgres',
	:password => node['postgresql']['password']['postgres']
}

# create the main database
postgresql_database 'feedreader' do
	connection postgresql_connection_info
	owner 'postgres'
	action :create
end

# create the database user that is used by the application
postgresql_database_user 'feedreader_app' do
	connection postgresql_connection_info
	password 'zUSAC7HbtXcVMkk'
	action :create
end