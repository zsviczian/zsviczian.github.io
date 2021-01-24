      function thankyou() {
        e = document.getElementsByClassName("MyFooter");
		  let s = '<table><tbody><tr>';
          s   += '<td><p style="margin-right: 24px;">Like this blog? <br/>Show your support.</p></td>';
          s   += '<td><sc' + 'ript type="text/javascript" src="https://ko-fi.com/widgets/widget_2.js"><';
          s   += '/script><sc'+'ript type="text/javascript">kofiwidget2.init("Buy me a coffee", "#eb6363", "D1D5302HF");kofiwidget2.draw();</';
          s   += 'script></td></tr></tbody></table>';
          e.innerHTML = s;
          console.log(s);
      }