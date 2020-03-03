function DmnCheckJsExtension(eventBus, drd, elementRegistry, moddle) {

    eventBus.on('elements.changed', (_) => {

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
                        log(shape);
                    });
                })
            });

            log("Validation finished.");
        });

    });
}



function log(...args) {
    console.log('[DmnCheckJsExtension]', ...args);
}

DmnCheckJsExtension.$inject = [ 'eventBus', 'drd', 'elementRegistry', 'moddle'];

module.exports = DmnCheckJsExtension;

