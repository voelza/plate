const plate = function() {
    function setup(selector, setupFunc) {
        document
        .querySelectorAll(selector)
        .forEach(element => {
            setupFunc({element});
        });
    }

    setup("[data-p-setup-post]", ({element}) => {
      function alertPostContent() {
        alert("Post Content!");
      }
    });
}();