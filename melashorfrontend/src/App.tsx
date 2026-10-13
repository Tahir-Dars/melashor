import {Component,type ReactNode} from "react"

class AppErrorBoundary extends Component<{ children: ReactNode},{error: unknown}>{
  override state ={error:null as unknown};


    static getDerivatedStateFromErro(error:unknown)
    {
      return (error);
    }

    override render(){
        if(this.state.error){
          
        }
    }
    
  };

function App() {

  return (
    <>
   :- MelaShor -:
    </>
  )
}

export default App
