const plate = function() {
    function setup(selector, setupFunc) {
        document
        .querySelectorAll(selector)
        .forEach(e => {
            const uuid=e.previousSibling?.textContent;
            setupFunc({element:e,props:plateModel[uuid]});
        });
    }

    setup('[data-p-setup-post]',({element}) => {
      function alertPostContent() {
        alert("Post Content!");
      }
    });
}();