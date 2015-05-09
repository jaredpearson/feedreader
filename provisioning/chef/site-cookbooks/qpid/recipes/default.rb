
package 'qpidd' do
	version '0.14-2'
	action :install
end

package 'qpid-tools' do
	version '0.12-1'
	action :install
end

directory '/etc/qpid' do
	owner 'root'
	group 'root'
	mode '0755'
end

directory '/var/lib/qpidd' do
	owner node['qpid']['user']
	group node['qpid']['group']
	mode '0750'
end

template '/etc/qpid/qpidd.conf' do
	source 'qpidd.conf.erb'
    notifies :restart, 'service[qpidd]'
end

template node['qpid']['qpidd']['acl-file'] do
	source 'qpidd.acl.erb'
    notifies :restart, 'service[qpidd]'
end 

template '/etc/sasl2/qpidd.conf' do
	source 'sasl-qpidd.conf.erb'
    notifies :restart, 'service[qpidd]'
end 

# add a user to the sasl db
bash 'add_user_feedreader' do
	user "root"
	code <<-EOH
	echo test | saslpasswd2 -c -p -f #{node['qpid']['sasl']['sasldb_path']} -u #{node['qpid']['qpidd']['realm']} feedreader
	touch '/tmp/qpid_user_feedreader'
    EOH
    not_if { ::File.exists?("/tmp/qpid_user_feedreader")}
    notifies :restart, 'service[qpidd]', :immediately
end

service 'qpidd' do 
	supports :restart => true, :reload => true
	action [ :enable, :start ]

	# recreate the queues every start/restart since there is no msgstore configured
    notifies :run, 'bash[add_feedrequest_queue]'
end

# add the feed.request queue
bash 'add_feedrequest_queue' do
	code <<-EOH
	qpid-config --durable add queue feed.request
	touch '/tmp/qpid_queue_feedrequest'
	EOH
end
