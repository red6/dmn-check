function DmnCheckJsExtension(eventBus, drd, elementRegistry, moddle, overlays) {

    eventBus.on('import.done', function() {
        validate();
    });


    eventBus.on('elements.changed', function() {
        validate();
    });

    function validate() {
        moddle.toXML(drd._definitions, {}, (err, xml) => {
            log("Start validation.");

            fetch('http://localhost:4567/validate', {
                method: "POST",
                body: xml
            }).then(res => {
                res.text().then(function (results) {
                    log("Request complete! response:", results);
                    JSON.parse(results).items.forEach(result => {
                        const shape = elementRegistry.get(result.id);

                        overlays.add(shape, 'badge', {
                            position: {
                                bottom: 0,
                                left: 0
                            },
                            html: '<div title="' + result.message + '" class="badge badge-' + result.severity.toLowerCase() + '"></div>'
                        });
                    });
                })
            });

            log("Validation finished.");
        });
    }
}



function log(...args) {
    console.log('[DmnCheckJsExtension]', ...args);
}

DmnCheckJsExtension.$inject = [ 'eventBus', 'drd', 'elementRegistry', 'moddle', 'overlays'];

module.exports = DmnCheckJsExtension;

