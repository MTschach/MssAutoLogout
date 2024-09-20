import React from 'react';
import './App.css';
import UserList from './UserList';
import UserChart from './UserChart';

function App() {
  const childRef = React.createRef<UserChart>();

  const loadUser = (userName:string, maxTime:number) => {
    if (childRef.current) {
      childRef.current.loadUser(userName, maxTime, "7");
    }
  }
  
    return (
    <div className="App">
      <header className="App-header">
         <UserList loadUser={(userName:string, maxTime:number)=>{ loadUser(userName, maxTime); }}/>
         <UserChart ref={childRef} />
      </header>
    </div>
  );
}

export default App;
