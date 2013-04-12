<?php
$allowedExts = array("csv");

$extension = end(explode(".", $_FILES["file"]["name"]));

if(($_FILES["file"]["size"] < 10000000)
        && in_array($extension, $allowedExts))
{
    if ($_FILES["file"]["error"] > 0) {
         //echo "Return Code: " . $_FILES["file"]["error"] . "<br>";  
    } else {
        //echo "Upload: " . $_FILES["file"]["name"] . "<br>";
        //echo "Type: " . $_FILES["file"]["type"] . "<br>";
        //echo "Size: " . ($_FILES["file"]["size"] / 1024) . " kB<br>";
        //echo "Temp file: " . $_FILES["file"]["tmp_name"] . "<br>";
        $filename = $_FILES["file"]["name"];

            move_uploaded_file($_FILES["file"]["tmp_name"],
                                "logs/" . $filename);
            echo "logs/" . $filename;
            echo "URL:" .$filename;
    }
} else {
    echo "ERROR";
    //echo "File size : " . $_FILES["file"]["size"];
}

$result = file_get_contents("http://ec2-54-234-7-38.compute-1.amazonaws.com/processFile.php?filename=".$filename);
?>
