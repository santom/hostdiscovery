<?php

$file=fopen("logs/".$_GET['filename'], "r") or exit("Unalble to open file");

$connect = mysqli_connect('localhost', 'root', 'cs2107', 'cg3204l');

if (mysqli_connect_errno($connect)) {
    echo "Failes to connect to MySQL: " . mysqli_connect_error();
}
//save info to clients table

$header = split(",",fgets($file));

//$client_id = str_replace(":","",$header[1]);
$client_id = time() / rand(0, 10000);

echo "$client_id, $header[0], $header[1], $header[2], $header[3], $header[4], $header[5]";

date_default_timezone_set('Asia/Kuala_Lumpur');
$my_date =date('Y-m-d H:i:s', time());;
$res = mysqli_query($connect, 
    "insert into clients (client_id, client_ip, mac, os, interface, date, packets_sum) 
        values ('$client_id', '$header[0]', '$header[1]', '$header[2]', 
                '$header[3]', '$my_date', $header[4])"
            );

if (!$res) {
    die ("Error: " . mysqli_error());
}

date_default_timezone_set('Asia/Kuala_Lumpur');

while(!feof($file)) {
    $line = fgets($file);
    echo $line . "<br>";
    $list = split(",", $line);
    $timestamp = time() / rand(0, 10000);
    if($list[0] == "")
        break;
    //save info to report table
    $date = date('Y-m-d H:i:s', time());
    $query = "insert into report (id, client_id, host_ip, host_mac, host_os, host_vendor, date)
              values ($timestamp, '$client_id', '$list[0]', '$list[1]', '$list[2]', '$list[3]', '$date')";
    
    mysqli_query($connect, $query);
}
 
mysqli_close($connect);
fclose($file);
?>  
