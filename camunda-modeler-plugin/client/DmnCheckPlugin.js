import { PureComponent } from 'camunda-modeler-plugin-helpers/react';

export default class DmnCheckPlugin extends PureComponent {

    constructor(props) {

        super(props);

        const {
            subscribe
        } = props;

        subscribe('dmn.modeler.created', (event) => {

            const {
                tab,
            } = event;

            fetch('http://localhost:4567/validate', {
                method: "POST",
                body: tab.file.contents
            }).then(res => {
                res.text().then(function (text) {
                    console.log("Request complete! response:", text);
                    alert(text);
                })
            });

            log('Modeler created for tab', tab);

        });

        subscribe('tab.saved', (event) => {
            const {
                tab
            } = event;

            fetch('http://localhost:4567/validate', {
                method: "POST",
                body: tab.file.contents
            }).then(res => {
                res.text().then(function (text) {
                    console.log("Request complete! response:", text);
                    alert(text);
                })
            });

            log('Tab saved', tab);
        });

    }

    render() {
        return null;
    }
}


// helpers //////////////
function log(...args) {
    console.log('[DmnCheckPlugin]', ...args);
}
