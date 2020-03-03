import {
  registerDmnJSPlugin,
} from 'camunda-modeler-plugin-helpers';

import DmnExtensionModule from './dmn-js-extension';

registerDmnJSPlugin(DmnExtensionModule, 'drd');
