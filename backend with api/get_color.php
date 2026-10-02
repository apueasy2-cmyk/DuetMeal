<?php $im = imagecreatefrompng('admin/assets/logo.png'); $rgb = imagecolorat($im, 0, 0); $r = ($rgb >> 16) & 0xFF; $g = ($rgb >> 8) & 0xFF; $b = $rgb & 0xFF; printf('#%02x%02x%02x', $r, $g, $b); ?>
