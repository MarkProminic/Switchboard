require 'yaml'
VAGRANTFILE_API_VERSION = "2"

required_plugins = %w(vagrant-share vagrant-vbguest)
unless Vagrant.has_plugin?("vagrant-vbguest")
  puts 'Installing vagrant-vbguest Plugin...'
  system('vagrant plugin install vagrant-vbguest')
end

 configuration = YAML::load(File.read("#{File.dirname(__FILE__)}/Hosts.yml"))
runPath = "#{File.dirname(__FILE__)}/" + configuration["path"]

require File.expand_path("#{runPath}/Hosts.rb")

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
	config.vm.boot_timeout = 1200
    Hosts.configure(config, configuration)
end
