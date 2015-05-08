name "rdbms"

default_attributes(
	"postgresql" => {
		"version" => "9.3",
		"enable_pgdg_apt" => true,
		"password" => {
			"postgres" => "rDVSPDaSXXg5J99"
		},
		"contrib" => {
			"packages" => [
				"pgcrypto"
			],
			"extensions" => [
				"pgcrypto"
			]
		},
		"config" => {
			"listen_addresses" => "*"
		},
		"pg_hba" => [
			{
				:type => 'host', 
				:db => 'all', 
				:user => 'postgres,feedreader_app', 
				:addr => '192.168.52.0/24', 
				:method => 'password'
			}
		]
	}
)

run_list(
	"recipe[apt]", 
	"recipe[postgresql]", 
	"recipe[postgresql::server]",
	"recipe[feedreader::database]"
)