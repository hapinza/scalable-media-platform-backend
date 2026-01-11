import {useState } from "react";
import api from "../api/axios";




function LoginPage(){
    const [loginId, setLoginId] = useState("");
    const [loginPassword, setLoginPassword] = useState("");
    const [message, setMessage] = useState("");

    const handelLogin = async(e) =>{
        e.preventDefault();

        try{
            const response = await api.post("/members/login", {
                loginId,
                loginPassword
    });

        if(response.data.success){
            setMessage("login complete!" + response.data.memberId);
        }
        else{
            setMessage("login fail" + response.data.message);
        }
    }catch(error){
        console.error(error);
        setMessage("server error");
    }


};

return(
<div classNmae = "flex flex col items-center mt-20">
<h1 className = "text-3xl font-bold">login</h1>

<form className = "felx flex-col w-80 mt-5" onSubmit = {
    handelLogin}>
<input type="text" 
placeholder = "Login ID"
className = "p-2 border mb-3"
value = {loginId}
onChnage = {(e) => setLoginId(e.target.value)}
/>



<input type="password" 
placeholder = "password"
className = "p-2 border mb-3"
value = {loginPassword}
onChange = {(e) => setLoginPassword(e.target.value)}
    />

<button className = "bg-red-600 text-white p-2 rounded ">
    Login
</button>
</form>

{message && <p calssName = "mt-4 text-red-500">{message}</p>}

</div>

);

}

export default LoginPage;

