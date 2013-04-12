<?php
$client_id = $_GET['id'];

$connect = mysqli_connect('localhost', 'root', 'cs2107', 'cg3204l');

if (mysqli_connect_errno($connect)) {
    echo "Failes to connect to MySQL: " . mysqli_connect_error();
}

// Query the vendor stats
$sql1 = "select host_vendor, count(*) as count from report where client_id='$client_id' group by host_vendor";

$result1 = mysqli_query($connect, $sql1);

$vendor_stats = array();

while ($row = mysqli_fetch_array($result1)) {
    $vendor_stats[$row['host_vendor']] = $row['count'];
}

// Query the os stats
$sql2 = "select host_os, count(*) as count from report where host_os != 'null' and client_id='$client_id' group by host_os";
$result2 = mysqli_query($connect, $sql2);

$os_stats = array();

while ($row = mysqli_fetch_array($result2)) {
    $os_stats[$row['host_os']] = $row['count'];
}

?>

<html>
  <head>
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

      }
    </script>
  </head>

  <body>
    <!--Div that will hold the pie chart-->
    <div id="chart1_div"></div>
    <div id="chart2_div"></div>
  </body>
</html>
