const SERVER_URL = 'http://localhost:42000/validate';

function DmnCheckJsExtension(eventBus, drd, elementRegistry, moddle, overlays, canvas) {

    const messages = document.createElement("div");
    messages.classList.add('message-box');
    messages.id = "message-box";
    messages.style.display = "none";
    canvas.getContainer().parentNode.appendChild(messages);

    eventBus.on('import.done', function() {
        validate();
    });


    eventBus.on('elements.changed', function() {
        validate();
    });

    function validate() {
        moddle.toXML(drd._definitions, {}).then((result) => {
            log("Start validation.");

            return fetch(SERVER_URL, {
                method: "POST",
                body: result.xml
            });
        }).then(res => res.json()).then(results => {
            log("Validation finished:", results);
            showResults(results.items);
        }).catch(error => {
            log("Validation failed:", error);
            showResults([{ message: "dmn-check server not reachable at " + SERVER_URL + " (" + error.message + ")" }]);
        });
    }

    function showResults(items) {
        const badgeCount = {};

        // Results of the previous validation are outdated
        overlays.remove({ type: 'dmn-check' });
        messages.replaceChildren();

        items.forEach(result => {
            const shape = elementRegistry.get(result.drgElementId);

            if (typeof shape !== "undefined") {
                // Build via DOM: messages contain text from the DMN file and must not be parsed as HTML
                const badge = document.createElement('div');
                badge.title = result.message;
                badge.className = 'badge badge-' + result.severity.toLowerCase();

                const index = badgeCount[result.drgElementId] || 0;
                overlays.add(shape, 'dmn-check', {
                    position: {
                        bottom: 0,
                        left: 21 * index
                    },
                    html: badge
                });

                badgeCount[result.drgElementId] = index + 1;
            } else {
                const message = document.createElement('div');
                message.textContent = result.message;
                messages.appendChild(message);
            }
        });

        messages.style.display = messages.hasChildNodes() ? "block" : "none";
    }
}



function log(...args) {
    console.log('[DmnCheckJsExtension]', ...args);
}

DmnCheckJsExtension.$inject = ['eventBus', 'drd', 'elementRegistry', 'moddle', 'overlays', 'canvas'];

module.exports = DmnCheckJsExtension;
