/**
 * This is for JS that is commonly used around our application.
 *
 * This file is part of EatSafe Saskatchewan.
 */

// Create fake console if one doesn't exist. For compatibility with dumbass HtmlUnit.
if(typeof console == "undefined") {
  window.console = {
    log: function () {},
    warn: function () {},
    error: function () {}
  };
}

/**
 * Displays an error message at the top of the user's viewport. This will last ~3 seconds before
 * disappearing on its own. Only one of these can be displayed at a time. Attempting to create another
 * will remove the previous.
 *
 * @param errorMessage Message to display.
 */
function createPageError(errorMessage) {
  // Destroy any existing errors
  $(".topViewError").fadeAndRemove("fast");

  var errorHtml = $('<div class="alert alert-danger alert-dismissible topViewError" role="alert" data-dismiss="alert">' +
      errorMessage + '</div>').hide();

  $(document.body).append(errorHtml);

  var errorDiv = $(".topViewError");
  errorDiv.fadeIn("slow", function() {
    window.setTimeout(function() {
      errorDiv.fadeAndRemove("slow");
    }, 3000);
  });
}

/**
 * Fades and then removes a DOM element once it has faded out.
 * @param duration The duration of the fade. Same settings as fadeOut.
 * @param callback A function called after the removal is done.
 */
jQuery.fn.fadeAndRemove = function(duration, callback) {
  this.fadeOut(duration || "slow", function() {
    this.remove();
    
    // Sane error handling: provide stack traces for the dumb schmuck who did this.
    if(typeof(callback) == "function") {
      callback(this);
    }
    else {
      console.log("Callback is not a function");
      console.trace();
    }
  });
}