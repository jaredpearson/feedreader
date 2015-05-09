name "mqueue"

default_attributes(
	"java" => {
		"install_flavor" => "oracle",
		"jdk_version" => "8",
		"oracle" => {
			"accept_oracle_download_terms" => true
		}
	}
)

run_list(
	"recipe[apt]",
	"recipe[java::default]",
	"recipe[qpid::default]"
)