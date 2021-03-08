(ns excalidraw.11
  (:require
   [clojure.set :as s]
   [reagent.core :as r]
   [roam.datascript :as rd]
   [roam.block :as block]
   [clojure.string :as str]
   [clojure.edn :as edn]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Common functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def silent false)
(defn debug [x] 
  (if-not silent (apply (.-log js/console) x)))

(defn get-arg-str [args]
  (str/replace 
   (str/replace 
    (str/replace 
     (str args) 
     #"\(#js" 
     "") 
    #"#js" 
    "") 
   #"\}\}\)" 
   "}}")
  )

(defn save-component [block-uid & args]
  (let [code-ref (->> (rd/q '[:find ?string .
                              :in $ ?uid
                              :where [?b :block/uid ?uid]
                         	         [?b :block/string ?string]]
                            block-uid)
                       (str)
                   	   (re-find #"\({2}.+\){2}"))
        args-str (get-arg-str args)
        render-string (str/join ["{{roam/render: " code-ref " "  args-str "}}"])]
    (debug  ["(save)  render-string: " render-string])
    (block/update 
      {:block {:uid block-uid
               :string render-string}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main Function Form-3
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def embedded-view "ev")
(def full-screen-view "fs")
(def embed-height 500)

(defn is-full-screen [cs]  ;;component-state
  (not= (:position @cs) embedded-view))

(defn set-view-mode-enabled [ew cs value] ;;exalidraw-wrapper component-state
  (debug ["(set-view-mode-enabled) " value])
  (swap! cs assoc-in [:view-mode] value)
  (if-not (nil? @ew) (.setViewModeEnabled @ew value)))

(defn set-zen-mode-enabled [ew cs value] ;;exalidraw-wrapper component-state
  (debug ["(set-zen-mode-enabled) " value])
  (swap! cs assoc-in [:zen-mode] value)
  (if-not (nil? @ew) (.setZenModeEnabled @ew value)))

(defn set-grid-mode-enabled [ew cs value] ;;exalidraw-wrapper component-state
  (debug ["(set-grid-mode-enabled) " value])
  (swap! cs assoc-in [:grid-mode] value)
  (if-not (nil? @ew) (.setGridModeEnabled @ew value)))

(defn resize [ew] 
  (debug ["(resize)"])
  (if-not (nil? @ew) (.onResize @ew)))

(defn zoom-to-fit [ew] 
  (debug ["(zoom-to-fit)"])
  (if-not (nil? @ew) (.zoomToFit @ew)))

(defn full-screen-keyboard-event-redirect [ew value]
  (debug ["(full-screen-keyboard-event-redirect)" value])
  (if-not (nil? @ew) (.fullScreenKeyboardEventRedirect @ew value)))

(defn update-scene [ew scene] 
  (debug ["(update-scene) scene: " scene])
  (.updateScene js/window.ExcalidrawWrapper @ew scene))

(defn get-drawing [ew] 
  (debug ["(get-drawing): " (.getDrawing js/window.ExcalidrawWrapper @ew)])
  (.getDrawing js/window.ExcalidrawWrapper @ew))

(defn going-full-screen? [ew cs x]
  (if (= x true)
    (do 
      (set-view-mode-enabled ew cs false)
      (set-zen-mode-enabled ew cs false)
      (full-screen-keyboard-event-redirect ew true))
    (do
      (set-view-mode-enabled ew cs true)
      (set-zen-mode-enabled ew cs true)
      (full-screen-keyboard-event-redirect ew false))))

(defn host-div-style [ew cs]
  (let [width    (.-innerWidth js/window)
        height   (.-innerHeight js/window)]
    (debug ["(host-div-style) cur-state :position " (:position @cs) " :height " height " :width " width " full-screen? " (is-full-screen cs)])
    (if (is-full-screen cs)
      {:position "fixed"
       :z-index 1000
       :top 50
       :left 50
       :width  "calc(100% - 100px)" ;;(- width 100) ;;
       :height (- height 100)}     
      {:position "relative"
       :width "100%"
       :height embed-height
       :display "block"
       :overflow "auto"})))

;;state to capture when callback confirms React libraries have loaded
(def deps-available (r/atom false))

(defn deps-ready-callback []
  (debug  ["(deps-ready-callback)"])
  (reset! deps-available true))

(defn main [{:keys [block-uid]} & args]
  (debug ["(main) component starting..."])
  (.excalidrawDependenciesCheck js/window deps-ready-callback)
  (if (= @deps-available false)
    [:div "Libraries have not yet loaded. Please refresh the block in a moment."]
    (fn []
      (debug ["(main) fn[] starting..."])
      (let [cs (r/atom {:position embedded-view  ;;component-state
                        :view-mode false
                        :zen-mode false
                        :grid-mode false})
           ew (r/atom nil) ;;excalidraw-wrapper
           drawing-before-edit (r/atom (first args))
           app-name (str/join ["excalidraw-app-" block-uid])]
        (debug ["(main) drawing-before-edit " @drawing-before-edit])
        (r/create-class 
         { :display-name "Excalidraw Roam Beta" 
           ;; Constructor      
           :constructor (fn [this props]
                          (debug ["(main) :constructor"]))
           :get-initial-state (fn [this]
                                (debug ["(main) :get-initial-state"]))
           ;; Static methods
           :get-derived-state-from-props (fn [props state] )
           :get-derived-state-from-error (fn [error] )
           ;; Methods
           :get-snapshot-before-update (fn [this old-argv new-argv] )
           :should-component-update (fn [this old-argv new-argv]
                                      (debug ["(main) :should-component-update"]))
           :component-did-mount (fn [this]
                                  (debug ["(main) :component-did-mount"])
                                  (reset! ew (js/ExcalidrawWrapper.
                                              app-name                               				 
                                              @drawing-before-edit))
                                  (debug ["(main) :component-did-mount Exalidraw mount initiated"])
                                  (set-view-mode-enabled ew cs true)
                                  (set-zen-mode-enabled ew cs true)
                                  (zoom-to-fit ew))
           :component-did-update (fn [this old-argv old-state snapshot]
                                   (debug ["(main) :component-did-update"])
                                   (resize ew))
           :component-will-unmount (fn [this])
           :component-did-catch (fn [this error info])
           :reagent-render (fn [{:keys [block-uid]} & args]
                             (debug ["(main) :reagent-render"])
                             [:div.excalidraw-host 
                              {:style (host-div-style ew cs 50)} 
                              [:div.button-wrapper
                               [:div.excalidraw-wrapper-buttons
                                [:button.excalidraw-wrapper-button
                                 {:draggable true
                                  :on-click (fn [e] 
                                              (if (is-full-screen cs) 
                                                (do (save-component block-uid (get-drawing ew))
                                                  (going-full-screen? ew cs false))
                                                (going-full-screen? ew cs true))
                                              (reset! drawing-before-edit (get-drawing ew))
                                              (swap! cs assoc-in [:position] 
                                                     (if (is-full-screen cs) embedded-view full-screen-view)))}
                                  (if (is-full-screen cs) "Save" "Edit")]
                               (if (is-full-screen cs) 
                                 [:button.excalidraw-wrapper-button 
                                  {:draggable true
                                   :on-click (fn [e] 
                                               (going-full-screen? ew cs false)
                                               (debug ["(main) Cancel :on-click before (update-scene)"])
                                               (update-scene ew @drawing-before-edit)
                                               (swap! 
                                                 cs 
                                                 assoc-in [:position] embedded-view)
                                               (debug ["(main) Cancel :on-click before (zoom-to-fit)"])
                                               (zoom-to-fit ew))}
                                  "Cancel"])]
                               [:div.excalidraw-wrapper-options
                                [:label.excalidraw-wrapper-label [:input.excalidraw-wrapper-checkbox
                                    {:type "checkbox"
                                     :checked (:zen-mode @cs)
                                     :on-change (fn [e] 
                                                  (set-zen-mode-enabled 
                                                   ew
                                                   cs
                                                   (not (:zen-mode @cs))))}] 
                                    "Zen Mode"]
                                [:label.excalidraw-wrapper-label [:input.excalidraw-wrapper-checkbox
                                  {:type "checkbox"
                                   :checked (:grid-mode @cs)
                                   :on-change (fn [e] 
                                                (set-grid-mode-enabled 
                                                 ew
                                                 cs
                                                 (not (:grid-mode @cs))))}] 
                                  "Grid Mode"]]]
                              [:div 
                               {:id app-name 
                                :style {:position "relative" :width "100%" :height "calc(100% - 30px)"}} 
                               ]])})))))

