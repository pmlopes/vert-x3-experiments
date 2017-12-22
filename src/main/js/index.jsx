import React from 'react';
import {render} from 'react-dom';
import {Blockchain} from './blockchain';

const Title = ({todoCount}) => {
  return (
    <div>
      <div>
        <h1>to-do ({todoCount})</h1>
      </div>
    </div>
  );
};

const TodoForm = ({addTodo}) => {
  // Input Tracker
  let input;
  // Return JSX
  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      addTodo(input.value);
      input.value = '';
    }}>
      <input className="form-control col-md-12" ref={node => {
        input = node;
      }}/>
      <br/>
    </form>
  );
};

const Todo = ({todo, remove}) => {
  // Each Todo
  return (<a href="#" className="list-group-item" onClick={() => {
    remove(todo.index)
  }}>{todo.data}</a>);
};

const TodoList = ({todos, remove}) => {
  // Map through the todos
  const todoNode = todos.map((todo) => {
    return (<Todo todo={todo} key={todo.index} remove={remove}/>)
  });
  return (<div className="list-group" style={{marginTop: '30px'}}>{todoNode}</div>);
};

// Container Component
window.id = 0;

class TodoApp extends React.Component {
  constructor(props) {
    // Pass props to parent class
    super(props);
    // Set initial state
    this.state = {
      data: []
    };
    this.bc = new Blockchain(window.location.host, 'xyz.jetdrone.blockchain');
  }

  // Lifecycle method
  componentDidMount() {
    let bc = this.bc;
    this.setState({data: bc.chain});

    bc.connect(() => {
      console.log('connected!');
    });
    bc.onBlock(() => {
      this.setState({data: bc.chain});
    });
    bc.onReplace(() => {
      this.setState({data: bc.chain});
    });
  }

  // Add handler
  addTodo(val) {
    // Add date to the blockchain
    this.bc.add(val, (err) => {
      if (!err) {
        this.setState({data: this.bc.chain});
      }
    });
  }

  // Handle remove
  handleRemove(id) {
    // Filter all todos except the one to be removed
    const remainder = this.state.data.filter((todo) => {
      if (todo.index !== id) return todo;
    });
    // Update state with filter
    axios.delete(this.apiUrl + '/' + id)
      .then((res) => {
        this.setState({data: remainder});
      })
  }

  render() {
    // Render JSX
    return (
      <div>
        <Title todoCount={this.state.data.length}/>
        <TodoForm addTodo={this.addTodo.bind(this)}/>
        <TodoList
          todos={this.state.data}
          remove={this.handleRemove.bind(this)}
        />
      </div>
    );
  }
}

render(<TodoApp/>, document.getElementById('container'));
