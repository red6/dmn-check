'use strict';

// Camunda Modeler greys out plugins without menu entries in its plugins menu
module.exports = function(electronApp, menuState) {
  return [{
    label: 'Documentation',
    action: function() {
      require('electron').shell.openExternal('https://github.com/red6/dmn-check#camunda-modeler-plugin');
    }
  }];
};
