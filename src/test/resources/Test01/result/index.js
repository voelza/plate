if(!plateModel){var plateModel = {};}
(() => {
    function setup(selector, setupFunc) {
        document
        .querySelectorAll(selector)
        .forEach(e => {
            const uuid=e.previousSibling?.textContent ?? 'main';
            setupFunc({element:e,props:plateModel[uuid]});
        });
    }

    setup('[data-p-setup-post]',({element,props}) => {
      function alertPostContent() {
        alert("Post Content!");
      }
    });
})();