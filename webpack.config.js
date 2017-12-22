const webpack = require('webpack');
const path = require('path');

const APP_DIR = path.resolve(__dirname, 'src/main/js');

module.exports = {
  module : {
    loaders : [
      {
        test : /\.jsx?/,
        include : APP_DIR,
        loader : 'babel-loader'
      }
    ]
  },

  entry: APP_DIR + '/index.jsx',
  output: {
    path: path.resolve(__dirname, 'src/main/resources/webroot'),
    filename: 'bundle.js'
  }
};
