# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  
  config.vm.define :postgres do |postgres|
    postgres.vm.box = "hashicorp/precise64"
    postgres.vm.network "private_network", ip: "192.168.52.13"
    postgres.vm.provider "virtualbox" do |vb|
      vb.name = "feedreader-postgres"
      vb.memory = 2048
    end
    
    postgres.vm.provision :shell, inline: 'apt-get update'

    # ruby is needed to install chef
    postgres.vm.provision :shell, inline: 'apt-get install build-essential ruby1.9.1-dev --no-upgrade --yes'
    postgres.vm.provision :shell, inline: 'gem install chef --version 11.16.4 --no-rdoc --no-ri'

    postgres.vm.provision "chef_solo" do |chef|
      chef.cookbooks_path = ["provisioning/chef/site-cookbooks", "provisioning/chef/cookbooks"]
      chef.roles_path = "provisioning/chef/roles"
      chef.add_role "rdbms"
    end
  end

  config.vm.define :qpid do |qpid|
    qpid.vm.box = "hashicorp/precise64"
    qpid.vm.network "private_network", ip: "192.168.52.12"
    qpid.vm.provider "virtualbox" do |vb|
      vb.name = "feedreader-qpid"
      vb.memory = 512
    end

    qpid.vm.provision :shell, inline: 'apt-get update'

    # ruby is needed to install chef
    qpid.vm.provision :shell, inline: 'apt-get install build-essential ruby1.9.1-dev --no-upgrade --yes'
    qpid.vm.provision :shell, inline: 'gem install chef --version 11.16.4 --no-rdoc --no-ri'

    qpid.vm.provision "chef_solo" do |chef|
      chef.cookbooks_path = ["provisioning/chef/site-cookbooks", "provisioning/chef/cookbooks"]
      chef.roles_path = "provisioning/chef/roles"
      chef.add_role "mqueue"
    end

  end

end