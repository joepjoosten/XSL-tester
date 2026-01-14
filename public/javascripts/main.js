// XSL-Tester Main JavaScript
// Converted from CoffeeScript to modern ES6+ JavaScript

const DONE_TYPING_INTERVAL = 1000;
let typingTimer = null;
let autotransform = true;

// Default XML content
const DEFAULT_XML = `<?xml version="1.0" encoding="UTF-8"?>
<body>
    <h1>Welcome to XSL Transform!</h1>
    <p>This is a modern XSLT transformation tool.</p>
    <ul>
        <li>Saxon 12 HE - Full XSLT 3.0/3.1 support</li>
        <li>Xalan 2.7.3 - XSLT 1.0 processor</li>
        <li>PDF generation with Apache FOP</li>
        <li>HTML preview</li>
    </ul>
</body>`;

// Default XSL content
const DEFAULT_XSL = `<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html" doctype-public="XSLT-compat" omit-xml-declaration="yes" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head>
                <title>Transformed Output</title>
            </head>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:transform>`;

// ACE Editor instances
let xmleditor, xsleditor, resulteditor;

// Analyze result to determine output type
function analyzeResult(data) {
    // Check for HTML doctype
    const htmlRegex = /^<!DOCTYPE\s+(HTML|html)/is;
    // Check for XML declaration
    const xmlRegex = /<\?xml\s+version\s*=\s*["']1\.0["']/;

    if (htmlRegex.test(data)) {
        htmlResult();
    } else if (xmlRegex.test(data)) {
        // Check for XSL-FO namespace
        const pdfNamespace = /xmlns:(\w+)\s*=\s*["']http:\/\/www\.w3\.org\/1999\/XSL\/Format["']/;
        const match = data.match(pdfNamespace);
        if (match) {
            // Check for XSL-FO root element
            const pdfRoot = new RegExp('<' + match[1] + ':root\\s', 's');
            if (pdfRoot.test(data)) {
                pdfResult();
            } else {
                plainResult();
            }
        } else {
            plainResult();
        }
    } else {
        plainResult();
    }
}

function pdfResult() {
    resultButton('PDF');
    if (document.querySelector('#resultview').style.display !== 'none') {
        pdfToIframe();
    }
}

function htmlResult() {
    resultButton('HTML');
    if (document.querySelector('#resultview').style.display !== 'none') {
        htmlToIframe();
    }
}

function plainResult() {
    const viewBtn = document.querySelector('#viewButton');
    if (viewBtn) viewBtn.remove();
    document.querySelector('#resultview').style.display = 'none';
    document.querySelector('#resulteditor').style.display = 'block';
}

function pdfToIframe() {
    document.querySelector('#files').submit();
}

function htmlToIframe() {
    const iframe = document.querySelector('#resultview iframe');
    let iframedoc = iframe.document;
    if (iframe.contentDocument) {
        iframedoc = iframe.contentDocument;
    } else if (iframe.contentWindow) {
        iframedoc = iframe.contentWindow.document;
    }
    iframedoc.open();
    iframedoc.writeln(document.querySelector('#result').value);
    iframedoc.close();
}

function resultButton(resultType) {
    const existing = document.querySelector('#viewButton');
    if (existing) existing.remove();

    const btn = document.createElement('div');
    btn.id = 'viewButton';
    btn.className = 'label-view';
    btn.dataset.resultType = resultType;
    btn.textContent = resultType;

    document.querySelector('#result-window').appendChild(btn);

    if (document.querySelector('#resultview').style.display !== 'none') {
        btn.classList.add('active');
    }
}

// Transform function
function transform() {
    const formData = new FormData(document.querySelector('#files'));

    fetch('/', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(data => {
        resulteditor.getSession().setValue(data);
        document.querySelector('#result').value = resulteditor.getSession().getValue();
        analyzeResult(data);
    })
    .catch(error => {
        console.error('Transform error:', error);
    });
}

// Update engine display
function updateEngine() {
    document.querySelector('#engine').value = engine;
    const engineLink = document.querySelector(`a[data-engine="${engine}"]`);
    if (engineLink) {
        document.querySelector('#engine-dropdown').textContent = engineLink.textContent;
    }
}

// Reset to default
function reset() {
    document.querySelector('#save span').textContent = 'Save';
    document.querySelector('#id_slug').value = '';

    xmleditor.getSession().setValue(DEFAULT_XML);
    document.querySelector('#xml').value = DEFAULT_XML;

    xsleditor.getSession().setValue(DEFAULT_XSL);
    document.querySelector('#xsl').value = DEFAULT_XSL;

    window.history.pushState('', 'XSL Transform', '/');
}

// Load fiddle by ID
function loadFiddle(fiddleId, fiddleRevision) {
    document.querySelector('#save span').textContent = 'Update';

    // Load XML
    fetch(`/xml/${fiddleId}?revision=${fiddleRevision}`)
        .then(response => response.text())
        .then(data => {
            xmleditor.getSession().setValue(data);
            document.querySelector('#xml').value = data;
        });

    // Load XSL
    fetch(`/xsl/${fiddleId}?revision=${fiddleRevision}`)
        .then(response => response.text())
        .then(data => {
            xsleditor.getSession().setValue(data);
            document.querySelector('#xsl').value = data;
        });
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Initialize ACE editors
    xmleditor = ace.edit('xmleditor');
    xmleditor.setTheme('ace/theme/monokai');
    xmleditor.getSession().setMode('ace/mode/xml');
    xmleditor.getSession().on('change', () => {
        document.querySelector('#xml').value = xmleditor.getSession().getValue();
        if (autotransform) {
            clearTimeout(typingTimer);
            typingTimer = setTimeout(transform, DONE_TYPING_INTERVAL);
        }
    });

    xsleditor = ace.edit('xsleditor');
    xsleditor.setTheme('ace/theme/monokai');
    xsleditor.getSession().setMode('ace/mode/xml');
    xsleditor.getSession().on('change', () => {
        document.querySelector('#xsl').value = xsleditor.getSession().getValue();
        if (autotransform) {
            clearTimeout(typingTimer);
            typingTimer = setTimeout(transform, DONE_TYPING_INTERVAL);
        }
    });

    resulteditor = ace.edit('resulteditor');
    resulteditor.setTheme('ace/theme/monokai');
    resulteditor.getSession().setMode('ace/mode/xml');
    resulteditor.setReadOnly(true);

    // Auto-transform toggle
    document.querySelector('#autotransform').addEventListener('click', (e) => {
        e.preventDefault();
        autotransform = !autotransform;
        const icon = document.querySelector('#autotransform i');
        icon.classList.toggle('fa-check-square');
        icon.classList.toggle('fa-square');
    });

    // New button
    document.querySelector('#new').addEventListener('click', (e) => {
        e.preventDefault();
        reset();
    });

    // Transform button
    document.querySelector('#transform').addEventListener('click', (e) => {
        e.preventDefault();
        if (!autotransform) {
            transform();
        }
    });

    // Save button
    document.querySelector('#save').addEventListener('click', (e) => {
        e.preventDefault();
        const formData = new FormData(document.querySelector('#files'));

        fetch('/save', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            const newUrl = data[1] === '1' ? '/' + data[0] : '/' + data[0] + '/' + data[1];
            window.history.pushState('', 'XSL Transform', newUrl);
            document.querySelector('#id_slug').value = data[0];
            document.querySelector('#save span').textContent = 'Update';
        })
        .catch(error => {
            console.error('Save error:', error);
            showAlert('Save Error', error.message);
        });
    });

    // Download button
    document.querySelector('#download').addEventListener('click', (e) => {
        e.preventDefault();
        const formData = new FormData(document.querySelector('#files'));

        fetch('/download', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text); });
            }
            // Get filename from Content-Disposition header
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = 'result.txt';
            if (contentDisposition) {
                const match = contentDisposition.match(/filename="(.+)"/);
                if (match) {
                    filename = match[1];
                }
            }
            return response.blob().then(blob => ({ blob, filename }));
        })
        .then(({ blob, filename }) => {
            // Create download link
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        })
        .catch(error => {
            console.error('Download error:', error);
            showAlert('Download Error', error.message);
        });
    });

    // Engine selection
    document.querySelectorAll('#engines a').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            engine = e.target.dataset.engine;
            updateEngine();
            transform();
        });
    });

    // Result window view/editor toggle
    document.querySelector('#result-window').addEventListener('click', (e) => {
        if (e.target.id === 'viewButton') {
            document.querySelector('#resulteditor').style.display = 'none';
            document.querySelector('#resultview').style.display = 'block';
            document.querySelector('#editorButton').classList.remove('active');
            document.querySelector('#viewButton').classList.add('active');

            const resultType = e.target.dataset.resultType;
            if (resultType === 'PDF') {
                pdfToIframe();
            } else if (resultType === 'HTML') {
                htmlToIframe();
            }
        } else if (e.target.id === 'editorButton') {
            document.querySelector('#resultview').style.display = 'none';
            document.querySelector('#resulteditor').style.display = 'block';
            document.querySelector('#editorButton').classList.add('active');
            const viewBtn = document.querySelector('#viewButton');
            if (viewBtn) viewBtn.classList.remove('active');
        }
    });

    // Initialize based on URL parameters
    if (typeof id !== 'undefined' && id !== '') {
        loadFiddle(id, revision);
    } else {
        reset();
    }

    updateEngine();
});

// Show alert
function showAlert(title, message) {
    const alert = document.querySelector('#alert');
    alert.querySelector('h4').textContent = title;
    alert.querySelector('p').textContent = message;
    alert.classList.add('show');

    setTimeout(() => {
        alert.classList.remove('show');
    }, 4000);
}
