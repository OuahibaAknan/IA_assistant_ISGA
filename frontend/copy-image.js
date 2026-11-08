const fs = require('fs');
const path = require('path');

const source = path.join(__dirname, 'public', 'imgpr.jpg');
const dest = path.join(__dirname, 'src', 'assets', 'imgpr.jpg');

fs.copyFileSync(source, dest);
console.log('Image copied successfully from public/ to src/assets/');
