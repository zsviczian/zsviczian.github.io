/*****************************************/
/*  event handler function to capture    */
/*  ctrl+z from reaching Roam when       */
/*  drawing is in full screen veiw       */
/*****************************************/
var DEBUG = true;

function myKeyboardListner(ev) {
  console.log(ev);
  if (ev.ctrlKey && (ev.code=='z' || ev.key=='z') ) 
    ev.preventDefault();
}

window['ExcalidrawWrapper'] = class {
  static notReadyToStart () {
    console.log("notReadyToStart()",(typeof Excalidraw == 'undefined') && (typeof ReactDOM == 'undefined') && (typeof React == 'undefined'));
    return (typeof Excalidraw == 'undefined') && (typeof ReactDOM == 'undefined') && (typeof React == 'undefined');
  }
  constructor (appName,initData) {    
    this.hostDIV = document.getElementById(appName);
    this.keyboardListner = null;
    ReactDOM.render(React.createElement(() => {
      const excalidrawRef = React.useRef(null);
      const excalidrawWrapperRef = React.useRef(null);
      const [dimensions, setDimensions] = React.useState({
        width: undefined,
        height: undefined
      });
      
      this.excalidrawRef = excalidrawRef;
      
      const [viewModeEnabled, setViewModeEnabled] = React.useState(false);
      const [zenModeEnabled, setZenModeEnabled] = React.useState(false);
      const [gridModeEnabled, setGridModeEnabled] = React.useState(false);

      this.setViewModeEnabled = setViewModeEnabled;
      this.setZenModeEnabled = setZenModeEnabled;
      this.setGridModeEnabled = setGridModeEnabled;

      React.useEffect(() => {
        setDimensions({
          width: excalidrawWrapperRef.current.getBoundingClientRect().width,
          height: excalidrawWrapperRef.current.getBoundingClientRect().height
        });
        const onResize = () => {
          setDimensions({
            width: excalidrawWrapperRef.current.getBoundingClientRect().width,
            height: excalidrawWrapperRef.current.getBoundingClientRect().height
          });
          this.zoomToFit();
        };

        window.addEventListener("resize", onResize);
        this.onResize = onResize;

        return () => window.removeEventListener("resize", onResize);
      }, [excalidrawWrapperRef]);

      return React.createElement(
        React.Fragment,
        null,
        React.createElement(
          "div",
          {
            className: "excalidraw-wrapper",
            ref: excalidrawWrapperRef
          },
          React.createElement(Excalidraw.default, {
            ref: excalidrawRef,
            width: dimensions.width,
            height: dimensions.height,
            initialData: initData,
            onChange: (elements, state) => {}, //console.log("Elements :", elements, "State : ", state),
            onPointerUpdate: (payload) => {},  //console.log(payload),
            onCollabButtonClick: () => {},     //window.alert("You clicked on collab button"),
            viewModeEnabled: viewModeEnabled,
            zenModeEnabled: zenModeEnabled,
            gridModeEnabled: gridModeEnabled
          })
        )
      );
    }), document.getElementById(appName));
    if (DEBUG) console.log("js: ExcalidrawWrapper.constructor() ReactDOM.render() initiated") ;
  }
  
  async zoomToFit() {
    if (DEBUG) console.log("js: ExcalidrawWrapper.zoomToFit() entering function");
    if(this.excalidrawRef != null) 
      if(this.excalidrawRef.current != null) {
        if (DEBUG) console.log("js: ExcalidrawWrapper.zoomToFit() excalidrawRef.current is available");
        const mainDIV = this.hostDIV.querySelector('main');
        let viewMode = this.excalidrawRef.current.getAppState().viewModeEnabled;
        if (viewMode) {
          this.setViewModeEnabled(false);
        }
        const e = new KeyboardEvent("keydown", {bubbles : true, cancelable : true, shiftKey : true, code:"Digit1"});
		mainDIV.dispatchEvent(e);
        if (viewMode) this.setViewModeEnabled(true);
      }
  }
  
  //this is a workaround because Roam catches some of the keys (e.g. CTRL+Z) before 
  //Exalidraw. When the application is in edit mode / full screen, sink all keyboar events and retrigger
  //to Excalidraw main div
  fullScreenKeyboardEventRedirect(isFullScreen) {
    if (isFullScreen) {
      document.addEventListener('keydown',myKeyboardListner);
      console.log("keyboard listener added");
    }
    else {
      document.removeEventListener('keydown',myKeyboardListner);
      console.log("keyboard listner removed");
    }
  }
                                                            
  
  static getDrawing(o) {
    if (DEBUG) console.log("js: ExcalidrawWrapper.getDrawing() entering function, object is available: ",(o!=null));
    if(o!=null) 
      return {elements: 
              o.excalidrawRef.current.getSceneElements(),
              appState: {
                viewBackgroundColor: o.excalidrawRef.current.getAppState().viewBackgroundColor,
                appearance: o.excalidrawRef.current.getAppState().appearance,
                gridSize: o.excalidrawRef.current.getAppState().gridSize}};
  }
  
  static updateScene(o,scene) {
    if (DEBUG) console.log("js: ExcalidrawWrapper.updateScene() entering function, object is available: ",(o!=null));
    if(o!=null) 
      o.excalidrawRef.current.updateScene(scene);
  }
}

const cssCode = `
      .excalidraw.excalidraw-modal-container {
        z-index: 1010 !important; 
      }
 
      kbd {
        color: black !important;
      }

      .popover {
        display: block;
        top: auto;
        left: auto;
        z-index: 1010 !important;
      }

      .excalidraw .App-menu_top .buttonList {
        display: flex;
      }

      .excalidraw-host {
        background: silver;       
       /* resize: vertical;*/
      }

      .excalidraw-wrapper {
        height: 100%;
        margin: 0px;
        position: relative;
      }

      .excalidraw-wrapper-button {
        height: 30px;
        border-radius: 3px;
        border: 1px;
        box-shadow: inset 0 0 0 1px rgb(16 22 26 / 20%), inset 0 -1px 0 rgb(16 22 26 / 10%);
        margin-right: 3px;
      }

      .excalidraw-wrapper-options {
        float: right;
        margin-right: 1px;
      }

      .excalidraw-wrapper-buttons {
        float: left;
        margin-left: 1px;
      }

      .excalidraw-wrapper-label {
        margin: 0px 8px 0px 0px;
        vertical-align: middle !important;
        position: relative;
        display: inline-block;
      }

      .excalidraw-wrapper-checkbox {
        margin: 0px 3px 0px 0px !important;
        vertical-align: middle !important;
        position: relative;
      }

      :root[dir="ltr"]
        .excalidraw
        .layer-ui__wrapper
        .zen-mode-transition.App-menu_bottom--transition-left {
        transform: none;
      }

      .button-wrapper {
        height: 30px;
      }`;

let styleElement = document.createElement('style');
styleElement.type = 'text/css';
if (styleElement.styleSheet) {
  styleElement.styleSheet.cssText = cssCode;
} else {
  styleElement.appendChild(document.createTextNode(cssCode));
}
document.getElementsByTagName("head")[0].appendChild(styleElement);