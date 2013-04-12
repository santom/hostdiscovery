<?php
$connect = mysqli_connect('localhost', 'root', 'cs2107', 'cg3204l');

if (mysqli_connect_errno($connect)) {
    echo "Failes to connect to MySQL: " . mysqli_connect_error();
}

$sql = "select client_id, client_ip, packets_sum, date from clients order by date";
$result = mysqli_query($connect, $sql);

$clients_list = array();
while ($row = mysqli_fetch_array($result)) {
    $result_row = array();
    array_push($result_row, $row['client_id']);
    array_push($result_row, $row['client_ip']);
    array_push($result_row, $row['packets_sum']);
    array_push($result_row, $row['date']);
    $clients_list[] = $result_row;
}

// Query the vendor stats
$sql1 = "select host_vendor, count(*) as count from report group by host_vendor";
$result1 = mysqli_query($connect, $sql1);

$vendor_stats = array();

while ($row = mysqli_fetch_array($result1)) {
    $vendor_stats[$row['host_vendor']] = $row['count'];
}

// Query the os stats
$sql2 = "select host_os, count(*) as count from report where host_os != 'null' group by host_os";
$result2 = mysqli_query($connect, $sql2);

$os_stats = array();

while ($row = mysqli_fetch_array($result2)) {
    $os_stats[$row['host_os']] = $row['count'];
}

// Query the hourly traffic
$sql3 = "select extract(hour from date) as h, count(*) as count from report group by h order by h";
$result3 = mysqli_query($connect, $sql3);

$hourly_stats = array();

for ($i = 0; $i<24; $i++) {
    $hourly_stats[$i] = 0;
}

while ($row = mysqli_fetch_array($result3)) {
    $hourly_stats[$row['h']] = $row['count'];
}

// Query hourly traffic by device
$sql3 = "select extract(hour from date) as h, count(*) as count from clients group by h order by h";
$result3 = mysqli_query($connect, $sql3);

$runs_by_hour = array();

for ($i = 0; $i<24; $i++) {
        $runs_by_hour[$i] = 1;
}

while ($row = mysqli_fetch_array($result3)) {
        $runs_by_hour[$row['h']] = $row['count'];
}

// Query daily traffic
$sql3 = "select date(date) as h, count(*) as count from report group by h order by h";
$result3 = mysqli_query($connect, $sql3);

$daily_stats = array();

while ($row = mysqli_fetch_array($result3)) {
        $daily_stats[$row['h']] = $row['count'];
}
?>

<html>
  <head>
    <link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">

      // Load the Visualization API and the piechart package.
      google.load('visualization', '1.0', {'packages':['corechart']});

      // Set a callback to run when the Google Visualization API is loaded.
      google.setOnLoadCallback(drawChart);

      // Callback that creates and populates a data table,
      // instantiates the pie chart, passes in the data and
      // draws it.
      function drawChart() {


        // Create the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Vendor');
        data.addColumn('number', 'Total');
        data.addRows([
<?php
        foreach ($vendor_stats as $vendor=>$count) {
            //echo "['".$row['host_vendor']."',". $row['count']."],";
            echo "['".$vendor."',". $count."],";
        }
?>
        ]);

        // Set chart options
        var options = {'title':'Statistics by Vendors',
                       'width':800,
                       'height':1500};

        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.BarChart(document.getElementById('chart1_div'));
        chart.draw(data, options);

        // Draw the OS Stats
        var data2 = new google.visualization.DataTable();
        data2.addColumn('string', 'OS');
        data2.addColumn('number', 'Total');
        data2.addRows([
<?php
        foreach ($os_stats as $os=>$count) {
            echo "['".$os."',". $count."],";
        }
?>
            ]);

        var options2 = {'title':'Statistics by Device Types',
                       'width':1000,
                       'height':400};

        var chart2 = new google.visualization.ColumnChart(document.getElementById('chart2_div'));
        chart2.draw(data2, options2);

        // Draw the hourly stats
        var data3 = new google.visualization.DataTable();
        data3.addColumn('string', 'OS');
        data3.addColumn('number', 'Total');
        data3.addRows([
<?php
        foreach ($hourly_stats as $hour=>$count) {
            echo "['".$hour."',". intval($count/$runs_by_hour[$hour])."],";
        }
?>
        ]);

        var options2 = {'title':'Average Hourly Connections'};

        var chart3 = new google.visualization.AreaChart(document.getElementById('chart3_div'));
        chart3.draw(data3,options2);

      }
    </script>
  </head>

  <body>
    <div class="title" style="margin:20px; font-size:25px; margin-left:37%; color:#4682B4;">Connection Scan Reports</div>
    <!--Div that will hold the pie chart-->
    <div id="table_div">
    <table class='table table-striped table-hover'>
    <tr>
    <th>ID</th>
    <th>IP Address</th>
    <th>Total Packets Scanned</th>
    <th>Date</th>
    <th>Result</th>
    </tr>
<?php
    foreach ($clients_list as $client) {
        echo '<tr>';
        foreach ($client as $idx) {
            echo '<td>' . $idx . '</td>';
        }
        echo '<td><a href="persniff.php?id='.$client[0].'">View result</a></td';
        echo '</tr>';
    }
?>
    </table>
    </div>
    <div id="chart1_div"></div>
    <div id="chart2_div"></div>
    <div id="chart3_div"></div>
  </body>
</html>
