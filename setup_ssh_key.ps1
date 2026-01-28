# PowerShell script to set up SSH key authentication
$password = ConvertTo-SecureString "Htht@1234" -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential ("root", $password)

$publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCokkVZc2E/PwnrHMQYSyJJs2VM3a08QNeTm49nXqc/koPxUaGwT1uksU9vh4sOwzzCrW6vVuocp6UScYW5T/sYS7B2/LAA3cjvtQ3mo2R0Yy3v3//51zhuxSBynINf6QvKLK99++WrYTzrbS57ulJT29nnyiDgKrAOhh8L2xnSUi0Ivh56tEWSXEFVaknMpBqwq3K6e8q1JqZCy4RNfwOIXW9dwRVwXFWvQC7DnnQtJtG2tmz3T/Cyoq4uS7W5DhnQCiefzAr6Tu8REersK3L6F76vcR/zgmoOw+I6WCVjCZSMX/pM12Fx2ze6315oPH3gmYDQEraKpkDRSWa8+T7iVLQPUO4uczDY/UotfQOVeIW7qCf0N45GZVyQikw+zHcsnv57m64gyr7hu6I0kpEcZn0uKXC0WMN5IFMtB5GDO2JHArSNOtc7BuJjbR91IBilhBvtaka5FJI7LVNuQ3V3Xu5wyUXintuirBBRmj7hfioIPISHqt38R1mgSwMOn5G2dBsYTy99bzaJ3+5HkqCth9PEyfji+pYvbBgkd8TSUtgXkjTT+C8LWFgzZU9JrIZBF115kV8sVkn8O8HDaXxl6J3DyZ1Qag00t6AtCWAJiSujosaTbesqFeUZdqgqOmDJY2D/pIvx+i8NRSc1y7EKqBeBCRPukrCRMKlrAa3QeQ== admin@DESKTOP-KC2OIL3"

# Using plink if available, otherwise use ssh with password
$sshCommand = @"
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo '$publicKey' >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
cat ~/.ssh/authorized_keys
"@

# Create a temporary expect script
$expectScript = @"
#!/usr/bin/expect -f
set timeout 30
spawn ssh -o StrictHostKeyChecking=no root@101.126.46.254
expect {
    "password:" {
        send "Htht@1234\r"
        expect "#"
        send "mkdir -p ~/.ssh\r"
        expect "#"
        send "chmod 700 ~/.ssh\r"
        expect "#"
        send "echo '$publicKey' >> ~/.ssh/authorized_keys\r"
        expect "#"
        send "chmod 600 ~/.ssh/authorized_keys\r"
        expect "#"
        send "exit\r"
    }
    "#" {
        send "mkdir -p ~/.ssh\r"
        expect "#"
        send "chmod 700 ~/.ssh\r"
        expect "#"
        send "echo '$publicKey' >> ~/.ssh/authorized_keys\r"
        expect "#"
        send "chmod 600 ~/.ssh/authorized_keys\r"
        expect "#"
        send "exit\r"
    }
}
expect eof
"@

$expectScript | Out-File -FilePath "temp_ssh_setup.exp" -Encoding ASCII

Write-Host "Running expect script..."
expect temp_ssh_setup.exp
Remove-Item temp_ssh_setup.exp
