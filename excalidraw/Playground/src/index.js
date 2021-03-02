//import "./styles.css";

const excalidrawWrapper = document.getElementById("excalidraw-app");

const props = {
  onChange: (data, appState) => {
    console.log(data, appState);
  }
};

/*eslint-disable */
ReactDOM.render(
  React.createElement(Excalidraw.default, props),
  excalidrawWrapper
);
