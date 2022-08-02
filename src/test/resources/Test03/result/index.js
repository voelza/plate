const plate = function() {
    function setup(selector, setupFunc) {
        document
        .querySelectorAll(selector)
        .forEach(e => {
            const uuid=e.previousSibling?.textContent;
            setupFunc({element:e,props:plateModel[uuid]});
        });
    }
    setup('[data-p-setup-test]',({element}) => {
      console.log("hello");
    });
    setup('[data-p-setup-header]',({element}) => {
          function buh() {
              alert("Buhh");
          }
    });
    setup('[data-p-setup-post]',({element}) => {
          function alertPostContent() {
              alert("Post Content!");
          }
    });
}();