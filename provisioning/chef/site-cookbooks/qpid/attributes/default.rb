
default['qpid'] = {
	'user' => 'qpid',
	'group' => 'qpid'
}

default['qpid']['qpidd'] = {
	'auth' => 'no',
	'realm' => 'QPID',
	'log-enable' => '',
	'acl-file' => '/etc/qpid/qpidd.acl'
}

default['qpid']['sasl'] = {
	'mech_list' => 'PLAIN ANONYMOUS',
	'sasldb_path' => '/etc/qpid/qpidd.sasldb'
}